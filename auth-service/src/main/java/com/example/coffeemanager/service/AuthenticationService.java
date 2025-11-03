package com.example.coffeemanager.service;


import com.example.coffeemanager.DTO.request.*;
import com.example.coffeemanager.DTO.response.AuthenticationResponse;
import com.example.coffeemanager.DTO.response.IntrospectReponse;
import com.example.coffeemanager.entity.Account;
import com.example.coffeemanager.entity.InvalidatedToken;
import com.example.coffeemanager.exception.AppException;
import com.example.coffeemanager.exception.ErrorCode;
import com.example.coffeemanager.repository.AccountRepository;
import com.example.coffeemanager.repository.InvalidatedRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticationService {
     AccountRepository accountRepository;
     InvalidatedRepository invalidatedRepository;


    @NonFinal
    @Value( "${jwt.signerKey}")
    protected String SECRET_KEY;

    @NonFinal
    @Value( "${jwt.valid-duration}")
    protected Long VALID_DURATION;

    @NonFinal
    @Value( "${jwt.refresh-duration}")
    protected Long REFRESH_DURATION;

    public IntrospectReponse introspect(IntrospectRequest request ) throws ParseException, JOSEException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e){
            isValid = false;
        }
        return IntrospectReponse.builder()
                .vaild(isValid)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        // 👇 Thay đổi logic: Tìm bằng Account
        var account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // Có thể đổi ErrorCode

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), account.getPassword() );

        if (!authenticated)
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        String token = generateToken(account); // 👇 Dùng account
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    // 👇 Thay đổi tham số từ User -> Account
    private String generateToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getUsername())
                // 👇 Thay đổi quan trọng: Gắn staffId vào token
                .claim("staffId", account.getStaffId())
                .issuer("your-issuer")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account)) // 👇 Dùng account
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error signing token", e);
            throw new RuntimeException(e);
        }
    }

    // 👇 Thay đổi tham số từ User -> Account và đơn giản hóa
    private String buildScope(Account account){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(account.getRoles())) {
            account.getRoles().forEach(role -> {
                stringJoiner.add(role.getName());
            });
        }
        // Bỏ qua logic "permissions" vì Role entity mới của chúng ta không có
        return stringJoiner.toString();
    }

    // Hàm này không cần thay đổi vì nó chỉ xác thực token
    private SignedJWT verifyToken(String token, boolean isrefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        var verified = signedJWT.verify(verifier);
        Date expirationTime = (isrefresh)
                ? new Date (signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!(verified && expirationTime.after(new Date())))
            throw new AppException(ErrorCode.INVALID_TOKEN);
        if(invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.INVALID_TOKEN);
        return signedJWT;
    }

    // Hàm này không cần thay đổi
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try{
            var signToken = verifyToken(request.getToken(), true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expirationTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expirationDate(expirationTime)
                    .build();
            invalidatedRepository.save(invalidatedToken);
        } catch (AppException e){
            log.error("Token is invalid or expired");
            throw e;
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var token = request.getToken();
        var signJWT = verifyToken(token, true);
        var jit = signJWT.getJWTClaimsSet().getJWTID();
        var expirationTime = signJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expirationDate(expirationTime)
                .build();
        invalidatedRepository.save(invalidatedToken);
        var username = signJWT.getJWTClaimsSet().getSubject();

        // 👇 Thay đổi logic: Tìm bằng Account
        var account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var newToken = generateToken(account); // 👇 Dùng account
        return AuthenticationResponse.builder()
                .token(newToken)
                .authenticated(true)
                .build();
    }

}
