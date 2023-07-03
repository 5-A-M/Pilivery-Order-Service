pipeline {
    agent any
    options {
        timeout(time: 1, unit: 'HOURS') // set timeout 1 hour
    }
    environment {
        TIME_ZONE = 'Asia/Seoul'
        PROFILE = 'local'

        REPOSITORY_CREDENTIAL_ID = credentials('GitCredential')
        REPOSITORY_URL = credentials('OrderServiceRepositoryUrl')
        TARGET_BRANCH = 'master'

        AWS_CREDENTIAL_NAME = credentials('AWSCredentials')
        ECR_PATH = credentials('ecrPath')
        IMAGE_NAME = 'order-service'
        REGION = credentials('region')
    }
    stages{
        stage('init') {
            steps {
                echo 'init stage'
                deleteDir()
            }
            post {
                success {
                    echo 'success init in pipeline'
                }
                failure {
                    error 'fail init in pipeline'
                }
            }
        }
        stage('clone project') {
            steps {
                git url: "$REPOSITORY_URL",
                    branch: "$TARGET_BRANCH",
                    credentialsId: "$REPOSITORY_CREDENTIAL_ID"
                sh "ls -al"
            }
            post {
                success {
                    echo 'success clone project'
                }
                failure {
                    error 'fail clone project' // exit pipeline
                }
            }
        }

        stage('clone secret file') {
            steps {
                withCredentials([file(credentialsId: 'pilivery-backend-application-yml', variable: 'secretFile')]) {
                    sh "pwd & mkdir /var/lib/jenkins/workspace/user-service/src/main/resources"
                    dir('./src/main/resources') {
                        sh "cp ${secretFile} /var/lib/jenkins/workspace/user-service/src/main/resources/application.yml"
                    }
                }
            }
        }

        stage('build project') {
            steps {
                sh '''
        		 ./gradlew clean build 
        		 '''
            }
            post {
                success {
                    echo 'success build project'
                }
                failure {
                    error 'fail build project' // exit pipeline
                }
            }
        }

        stage('docker build and push to ecr') {
            steps {
                script{
                    // cleanup current user docker credentials
                    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'

                    echo "Success Delete Docker Config"

                    docker.withRegistry("https://${ECR_PATH}", "ecr:${REGION}:AWSCredentials") {
                      def image = docker.build("${ECR_PATH}/${IMAGE_NAME}:${env.BUILD_NUMBER}")
                      image.push()
                    }

                }
            }
            post {
                success {
                    echo 'success upload image'
                }
                failure {
                    error 'fail upload image' // exit pipeline
                }
            }
        }
    }
}
