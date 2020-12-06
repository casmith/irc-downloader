pipeline {
    agent none

    environment {
        USERNAME = credentials('marvin-github-username');
        TOKEN = credentials('marvin-github-token')
        registry = 'casmith/marvinbot'
        registryCredential = 'dockerhub'
    }

    stages {
        stage('build') {
            agent { docker { image 'gradle:6.3.0-jdk11' } }
            steps {
                sh 'gradle dist'
            }
            post { always { stash includes: '**/*', name: 'build' } }
        }
        stage('publish image') {
            agent any
            steps {
                script {
                    unstash 'build'
                    sh 'ls -lah'
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                    //dockerImagePi = docker.build(registry + ":${BUILD_NUMBER}-rpi", "Dockerfile.rpi")
                    docker.withRegistry( '', registryCredential ) {
                        dockerImage.push()
                        dockerImage.push('latest')
                      //  dockerImagePi.push('latest-rpi')
                    }
                    sh "docker rmi $registry:$BUILD_NUMBER"
                    //sh "docker rmi $registry:$BUILD_NUMBER-rpi"
                }
            }
        }
        stage('deploy') {
            if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop') {
                build '../docker-syno/master'
            }
        }
    }
}
