// pipeline {
//     agent any
    
//     tools {
//         jdk 'JDK-21'
//         maven 'maven-3'
//     }

//     options {
//         buildDiscarder(logRotator(numToKeepStr: '10'))
//         disableConcurrentBuilds()
//     }

//     stages {
//         stage('Security: Gitleaks Scan') {
//             steps {
//                 script {
//                     echo "Running Gitleaks to detect hardcoded secrets..."
//                     sh 'docker run --rm -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v || true'
//                 }
//             }
//         }

//         stage('Install Root POM') {
//             steps {
//                 echo "Installing Root POM to local repository..."
//                 sh 'mvn clean install -N' 
//             }
//         }

//         stage('Build Common Library') {
//             steps {
                
//                 dir('common-library') { 
//                     echo "Building and installing common-library to local Maven repo..."
//                     sh 'mvn clean install -DskipTests'
//                 }
//             }
//         }

//         stage('Monorepo Services CI') {
//             matrix {
//                 axes {
//                     axis {
//                         name 'SERVICE'
//                         values 'media', 'product', 'cart', 'order', 'payment', 
//                                'search', 'storefront', 'storefront-bff', 'backoffice', 
//                                'backoffice-bff', 'customer', 'inventory', 'delivery', 
//                                'identity', 'location', 'promotion', 'rating', 
//                                'recommendation', 'sampledata', 'tax', 'webhook'
//                     }
//                 }
                
//                 stages {
//                     stage('Service CI') {
//                         when { 
//                             changeset "${SERVICE}/**" 
//                         }
//                         stages {
//                             stage('Test & Coverage') {
//                                 steps {
//                                     dir("${SERVICE}") {
//                                         sh 'mvn clean test jacoco:report'
//                                     }
//                                 }
//                                 post {
//                                     always {
//                                         dir("${SERVICE}") {
//                                             junit 'target/surefire-reports/*.xml'

//                                             jacoco(
//                                                 execPattern: 'target/jacoco.exec',
//                                                 classPattern: 'target/classes',
//                                                 sourcePattern: 'src/main/java',
//                                                 inclusionPattern: '**/*.class',
//                                                 minimumLineCoverage: '70', 
//                                                 changeBuildStatus: true
//                                             )
//                                         }
//                                     }
//                                 }
//                             }
                            
//                             stage('Code Quality & SAST') {
//                                 steps {
//                                     dir("${SERVICE}") {
//                                         withSonarQubeEnv('SonarQube-Server') {
//                                             sh 'mvn sonar:sonar'
//                                         }
//                                     }
//                                 }
//                             }

//                             stage('Build') {
//                                 steps {
//                                     dir("${SERVICE}") {
//                                         sh 'mvn package -DskipTests'
//                                     }
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }
//         }
//     }
// }
pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'maven-3'
        nodejs 'NodeJS-20' 
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Security: Gitleaks Scan') {
            steps {
                script {
                    echo "Running Gitleaks to detect hardcoded secrets..."
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                        sh 'docker run --rm -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v'
                    }
                }
            }
        }

        stage('Backend Services CI (Java)') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values 'media', 'product', 'cart', 'order', 'payment', 
                               'search', 'customer', 'inventory', 'delivery', 
                               'identity', 'location', 'promotion', 'rating', 
                               'recommendation', 'tax', 'webhook'
                    }
                }
                
                stages {
                    stage('Java CI') {
                        // Cú pháp changeset CHUẨN để bắt sự thay đổi của service
                        when { 
                            anyOf {
                                changeset pattern: "${SERVICE}/**/*", comparator: 'GLOB'
                                changeset pattern: "common-library/**/*", comparator: 'GLOB'
                                changeset pattern: "pom.xml", comparator: 'GLOB'
                            }
                        }
                        stages {
                            stage('Build, Test, SAST & Coverage') {
                                steps {
                                    // BƯỚC 1: Build ở thư mục gốc (không dùng dir) để Maven resolve dependencies đúng cách
                                    sh "mvn clean verify -pl ${SERVICE} -am"
                                    
                                    // BƯỚC 2: Đã dùng dir() để di chuyển vào trong thư mục service trước khi quét SonarQube
                                    dir("${SERVICE}") {
                                        withSonarQubeEnv('SonarCloud') {
                                            sh """
                                                mvn sonar:sonar \
                                                -Dsonar.projectKey=ITs-GiaHuy_yas \
                                                -Dsonar.organization=its-giahuy \
                                                -Dsonar.host.url=https://sonarcloud.io
                                            """
                                        }
                                    }
                                    
                                    // BƯỚC 3: Chạy Snyk bên trong thư mục của service
                                    dir("${SERVICE}") {
                                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                            sh 'npx snyk test -d --all-projects --severity-threshold=high'
                                        }
                                    }
                                }
                                post {
                                    always {
                                        dir("${SERVICE}") {
                                            junit 'target/surefire-reports/*.xml'
                                            
                                            // TODO: Tạm thời comment block JaCoCo để bypass lỗi DSL method.
                                            // Hướng dẫn: Chỉ bỏ comment (uncomment) phần này SAU KHI bạn đã cài đặt "JaCoCo plugin" trên Jenkins server.
                                            
                                            jacoco(
                                                execPattern: 'target/jacoco.exec',
                                                classPattern: 'target/classes',
                                                sourcePattern: 'src/main/java',
                                                inclusionPattern: '**/*.class',
                                                minimumLineCoverage: '70', 
                                                changeBuildStatus: true
                                            )
                                            
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Frontend & BFF CI (Node.js)') {
            matrix {
                axes {
                    axis {
                        name 'UI_SERVICE'
                        values 'storefront', 'storefront-bff', 'backoffice', 'backoffice-bff'
                    }
                }
                stages {
                    stage('Node.js CI') {
                        when { 
                            changeset pattern: "${UI_SERVICE}/**/*", comparator: 'GLOB'
                        }
                        stages {
                            stage('Install, Lint, Test, Scan & Build') {
                                steps {
                                    dir("${UI_SERVICE}") {
                                        echo "Building UI/BFF Project: ${UI_SERVICE}..."
                                        sh 'npm ci'
                                        sh 'npm run lint'
                                        
                                        sh 'npm run test'
                                        sh 'npm run build'

                                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                            sh 'npx snyk test'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}