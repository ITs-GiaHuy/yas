pipeline {
    agent any

    options {
        // Lưu trữ tối đa 10 build gần nhất để tiết kiệm ổ đĩa
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Thời gian chờ tối đa cho toàn bộ pipeline
        timeout(time: 2, unit: 'HOURS')
    }

    environment {
        // Cấu hình Credentials trong Jenkins quản lý
        SONAR_TOKEN = credentials('sonar-cloud-token')
        SNYK_TOKEN  = credentials('snyk-api-token')
        JAVA_HOME   = '/usr/lib/jvm/java-21'
    }

    stages {
        stage('Pre-check & Security') {
            parallel {
                stage('Gitleaks Scan') {
                    steps {
                        echo "Checking for leaked secrets..."
                        // Yêu cầu 7c: Quét lỗ hổng bảo mật (Secrets)
                        sh 'gitleaks detect --source . --verbose'
                    }
                }
                stage('Snyk Security Scan') {
                    steps {
                        echo "Scanning dependencies for vulnerabilities..."
                        // Yêu cầu 7c: Quét thư viện lỗi thời/nguy hiểm
                        sh 'snyk auth $SNYK_TOKEN'
                        sh 'snyk test --all-projects'
                    }
                }
            }
        }

        stage('Microservices Pipeline') {
            parallel {
                // 1. MEDIA SERVICE
                stage('Media Service') {
                    when {
                        // Yêu cầu 6: Chỉ chạy khi có thay đổi trong thư mục media
                        changeset "media/**"
                    }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('media') {
                                    // Chạy test và tạo báo cáo Jacoco
                                    sh 'mvn clean test'
                                }
                            }
                            post {
                                always {
                                    // Yêu cầu 5: Upload kết quả test & Coverage
                                    junit 'media/target/surefire-reports/*.xml'
                                    // Yêu cầu 7b: Fail build nếu coverage < 70%
                                    jacoco(
                                        execPattern: 'media/target/jacoco.exec',
                                        classPattern: 'media/target/classes',
                                        sourcePattern: 'media/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('media') {
                                    // Yêu cầu 7c: SonarCloud/SonarQube
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-media -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('media') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 2. PRODUCT SERVICE
                stage('Product Service') {
                    when { changeset "product/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('product') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'product/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'product/target/jacoco.exec',
                                        classPattern: 'product/target/classes',
                                        sourcePattern: 'product/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('product') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-product -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('product') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 3. CART SERVICE
                stage('Cart Service') {
                    when { changeset "cart/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('cart') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'cart/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'cart/target/jacoco.exec',
                                        classPattern: 'cart/target/classes',
                                        sourcePattern: 'cart/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('cart') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-cart -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('cart') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 4. CUSTOMER SERVICE
                stage('Customer Service') {
                    when { changeset "customer/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('customer') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'customer/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'customer/target/jacoco.exec',
                                        classPattern: 'customer/target/classes',
                                        sourcePattern: 'customer/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('customer') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-customer -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('customer') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 5. INVENTORY SERVICE
                stage('Inventory Service') {
                    when { changeset "inventory/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('inventory') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'inventory/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'inventory/target/jacoco.exec',
                                        classPattern: 'inventory/target/classes',
                                        sourcePattern: 'inventory/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('inventory') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-inventory -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('inventory') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 6. LOCATION SERVICE
                stage('Location Service') {
                    when { changeset "location/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('location') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'location/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'location/target/jacoco.exec',
                                        classPattern: 'location/target/classes',
                                        sourcePattern: 'location/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('location') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-location -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('location') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 7. ORDER SERVICE
                stage('Order Service') {
                    when { changeset "order/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('order') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'order/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'order/target/jacoco.exec',
                                        classPattern: 'order/target/classes',
                                        sourcePattern: 'order/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('order') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-order -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('order') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 8. PAYMENT SERVICE
                stage('Payment Service') {
                    when { changeset "payment/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('payment') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'payment/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'payment/target/jacoco.exec',
                                        classPattern: 'payment/target/classes',
                                        sourcePattern: 'payment/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('payment') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-payment -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('payment') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 9. PAYMENT-PAYPAL SERVICE
                stage('Payment-PayPal Service') {
                    when { changeset "payment-paypal/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('payment-paypal') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'payment-paypal/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'payment-paypal/target/jacoco.exec',
                                        classPattern: 'payment-paypal/target/classes',
                                        sourcePattern: 'payment-paypal/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('payment-paypal') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-payment-paypal -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('payment-paypal') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 10. PROMOTION SERVICE
                stage('Promotion Service') {
                    when { changeset "promotion/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('promotion') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'promotion/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'promotion/target/jacoco.exec',
                                        classPattern: 'promotion/target/classes',
                                        sourcePattern: 'promotion/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('promotion') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-promotion -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('promotion') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 11. RATING SERVICE
                stage('Rating Service') {
                    when { changeset "rating/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('rating') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'rating/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'rating/target/jacoco.exec',
                                        classPattern: 'rating/target/classes',
                                        sourcePattern: 'rating/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('rating') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-rating -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('rating') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 12. RECOMMENDATION SERVICE
                stage('Recommendation Service') {
                    when { changeset "recommendation/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('recommendation') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'recommendation/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'recommendation/target/jacoco.exec',
                                        classPattern: 'recommendation/target/classes',
                                        sourcePattern: 'recommendation/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('recommendation') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-recommendation -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('recommendation') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 13. SEARCH SERVICE
                stage('Search Service') {
                    when { changeset "search/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('search') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'search/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'search/target/jacoco.exec',
                                        classPattern: 'search/target/classes',
                                        sourcePattern: 'search/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('search') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-search -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('search') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 14. TAX SERVICE
                stage('Tax Service') {
                    when { changeset "tax/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('tax') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'tax/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'tax/target/jacoco.exec',
                                        classPattern: 'tax/target/classes',
                                        sourcePattern: 'tax/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('tax') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-tax -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('tax') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 15. WEBHOOK SERVICE
                stage('Webhook Service') {
                    when { changeset "webhook/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('webhook') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'webhook/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'webhook/target/jacoco.exec',
                                        classPattern: 'webhook/target/classes',
                                        sourcePattern: 'webhook/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('webhook') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-webhook -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('webhook') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 16. BACKOFFICE-BFF SERVICE
                stage('Backoffice-BFF Service') {
                    when { changeset "backoffice-bff/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('backoffice-bff') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'backoffice-bff/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'backoffice-bff/target/jacoco.exec',
                                        classPattern: 'backoffice-bff/target/classes',
                                        sourcePattern: 'backoffice-bff/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('backoffice-bff') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-backoffice-bff -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('backoffice-bff') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 17. STOREFRONT-BFF SERVICE
                stage('Storefront-BFF Service') {
                    when { changeset "storefront-bff/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('storefront-bff') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'storefront-bff/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'storefront-bff/target/jacoco.exec',
                                        classPattern: 'storefront-bff/target/classes',
                                        sourcePattern: 'storefront-bff/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('storefront-bff') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-storefront-bff -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('storefront-bff') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }

                // 18. COMMON LIBRARY (Nếu các service khác phụ thuộc vào nó)
                stage('Common Library') {
                    when { changeset "common-library/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('common-library') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'common-library/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'common-library/target/jacoco.exec',
                                        classPattern: 'common-library/target/classes',
                                        sourcePattern: 'common-library/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('common-library') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-common-library -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('common-library') { sh 'mvn install -DskipTests' }
                            }
                        }
                    }
                }

                // 19. DELIVERY SERVICE
                stage('Delivery Service') {
                    when { changeset "delivery/**" }
                    stages {
                        stage('Test & Coverage') {
                            steps {
                                dir('delivery') { sh 'mvn clean test' }
                            }
                            post {
                                always {
                                    junit 'delivery/target/surefire-reports/*.xml'
                                    jacoco(
                                        execPattern: 'delivery/target/jacoco.exec',
                                        classPattern: 'delivery/target/classes',
                                        sourcePattern: 'delivery/src/main/java',
                                        minimumInstructionCoverage: '70',
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                        stage('Static Analysis') {
                            steps {
                                dir('delivery') {
                                    sh 'mvn sonar:sonar -Dsonar.projectKey=yas-delivery -Dsonar.organization=your-org -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('delivery') { sh 'mvn package -DskipTests' }
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "CI Hoàn tất thành công cho các service có thay đổi!"
        }
        failure {
            echo "CI Thất bại. Vui lòng kiểm tra lại Code Quality hoặc Test Coverage!"
        }
    }
}
