pipeline {
    agent any
    options {
        timeout(time: 1, unit: 'HOURS') // set timeout 1 hour
    }
    environment {
        TIME_ZONE = 'Asia/Seoul'
        PROFILE = 'local'

        REPOSITORY_CREDENTIAL_ID = credentials('GitCredential')
        GIT_EMAIL = credentials('GitEmail')
        GIT_USERNAME = credentials('GitUsername')

        REPOSITORY_URL = credentials('OrderServiceRepositoryUrl')
        TARGET_BRANCH = 'master'

        HELM_REPOSITORY_URL = credentials('HELM_REPOSITORY_URL')
        HELM_TARGET_BRANCH = credentials('HELM_TARGET_BRANCH')

        AWS_CREDENTIAL_NAME = credentials('AWSCredentials')
        ECR_PATH = credentials('ecrPath')
        IMAGE_NAME = 'order-service'
        REGION = credentials('region')

        APP_VERSION = '1.0.'
    }
    stages{
        stage('Clean Workspace') {
            steps {
                echo '*********Clean Workspace*********'
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
        stage('Git Clone Application Code') {
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

        stage('Clone Application Secret') {
            steps {
                withCredentials([file(credentialsId: 'pilivery-backend-application-yml', variable: 'secretFile')]) {
                    sh "pwd & mkdir /var/lib/jenkins/workspace/${IMAGE_NAME}/src/main/resources"
                    dir('./src/main/resources') {
                        sh "cp ${secretFile} /var/lib/jenkins/workspace/${IMAGE_NAME}/src/main/resources/application.yml"
                    }
                }
            }
        }

        stage('Build Application') {
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

        stage('Docker Build And Push To ECR') {
            steps {
                script{
                    // cleanup current user docker credentials
                    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'

                    echo "Success Delete Docker Config"

                    docker.withRegistry("https://${ECR_PATH}", "ecr:${REGION}:AWSCredentials") {
                      def image = docker.build("${ECR_PATH}/${IMAGE_NAME}:${APP_VERSION}${env.BUILD_NUMBER}")
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

        stage("Clean Helm Workspace") {
            steps {
                script {
                    def helmWorkSpacePath = "/var/lib/jenkins/workspace/helm"
                    dir(helmWorkSpacePath) {
                        echo '*********Clean Workspace*********'
                        deleteDir()
                    }
                }
            }

            post {
                success {
                    echo 'success clean workspace'
                }
                failure {
                    error 'fail clean workspace' // exit pipeline
                }
            }
        }

        stage('Git Clone Helm Chart Repository') {
            steps {
                script {
                    def helmWorkSpacePath = "/var/lib/jenkins/workspace/helm"
                    dir(helmWorkSpacePath) {
                        git url: "$HELM_REPOSITORY_URL",
                            branch: "$HELM_TARGET_BRANCH",
                            credentialsId: "$REPOSITORY_CREDENTIAL_ID"
                        sh "ls -al"
                    }
                }
            }
        }

        stage('Update Helm Chart And Push') {
            steps {
                script {
                    def helmWorkSpacePath = "/var/lib/jenkins/workspace/helm"
                    dir(helmWorkSpacePath) {
                      sh "git remote add helm ${HELM_REPOSITORY_URL}"
                      sh "sed -i 's/tag: .*/tag: ${APP_VERSION}${env.BUILD_NUMBER}/' ${IMAGE_NAME}/values.yaml"
                      sh "git config user.email '${GIT_EMAIL}'"
                      sh "git config user.name '${GIT_USERNAME}'"
                      sh "git add ${IMAGE_NAME}/values.yaml"
                      sh "git commit -m 'Update Helm Chart ${IMAGE_NAME}:${APP_VERSION}${env.BUILD_NUMBER}'"
                      sh "git push helm ${HELM_TARGET_BRANCH}"
                    }
                }
            }

            post {
                success {
                    echo 'Success Change Helm Chart And Push'
                }
                failure {
                    error 'Fail to Change Helm Chart'
                }
            }
        }
    }
}
