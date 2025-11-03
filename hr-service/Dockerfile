# ----- Giai đoạn 1: Build ứng dụng (dùng JDK 21) -----
# Dùng image Maven chính thức với Eclipse Temurin (OpenJDK) 21
FROM maven:3-eclipse-temurin-21 AS build

# Đặt thư mục làm việc
WORKDIR /app

# Copy file pom.xml và tải dependencies (để tận dụng cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ mã nguồn và build
COPY src ./src
RUN mvn clean package -DskipTests

# ----- Giai đoạn 2: Tạo image để chạy (dùng JRE 21) -----
# Dùng image JRE 21 nhỏ gọn (alpine)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy file .jar đã được build từ giai đoạn 1
COPY --from=build /app/target/*.jar app.jar

# Mở port 8080 (port Spring Boot mặc định)
EXPOSE 8080

# Lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]