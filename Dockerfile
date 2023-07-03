FROM openjdk:11
WORKDIR /app
COPY /build/libs/order-service-1.0.jar orderSVC.jar
CMD ["java", "-jar", "/app/orderSVC.jar"]
