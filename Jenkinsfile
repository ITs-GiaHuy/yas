pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'maven-3'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        REVISION = '1.0-SNAPSHOT'
    }

    stages {
        stage('Security: Gitleaks Scan') {
            steps {
                script {
                    echo "Running Gitleaks to detect hardcoded secrets..."
                    sh 'docker run --rm -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v || true'
                }
            }
        }

        stage('Install Root POM') {
            steps {
                echo "Installing Root POM to local repository..."
                sh "mvn clean install -N -Drevision=${REVISION}" 
            }
        }

        stage('Build Common Library') {
            steps {
                dir('common-library') { 
                    echo "Building and installing common-library to local Maven repo..."
                    sh "mvn clean install -DskipTests -Drevision=${REVISION}"
                }
            }
        }

        stage('Monorepo Services CI') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values 'media', 'product', 'cart', 'order', 'payment', 
                               'search', 'storefront', 'storefront-bff', 'backoffice', 
                               'backoffice-bff', 'customer', 'inventory', 'delivery', 
                               'identity', 'location', 'promotion', 'rating', 
                               'recommendation', 'sampledata', 'tax', 'webhook'
                    }
                }
                
                stages {
                    stage('Service CI') {
                        when { 
                            changeset "${SERVICE}/**" 
                        }
                        stages {
                            stage('Test & Coverage') {
                                steps {
                                    dir("${SERVICE}") {
                                        sh "mvn clean test jacoco:report -Drevision=${REVISION}"
                                    }
                                }
                                post {
                                    always {
                                        dir("${SERVICE}") {
                                            junit 'target/surefire-reports/*.xml'
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
                            
                            stage('Code Quality & SAST') {
                                steps {
                                    dir("${SERVICE}") {
                                        withSonarQubeEnv('SonarQube-Server') {
                                            sh "mvn sonar:sonar -Drevision=${REVISION}"
                                        }
                                    }
                                }
                            }

                            stage('Build') {
                                steps {
                                    dir("${SERVICE}") {
                                        sh "mvn package -DskipTests -Drevision=${REVISION}"
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