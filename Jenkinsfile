pipeline {
  agent none
  environment {
    DOCKHUB_USERNAME = 'dockerhubbo'
  }
  stages {
    stage('maven Build src') {
      agent {
        docker {
          image 'maven:3-alpine'
          args '-v /root/.m2:/root/.m2'
        }
      }
      steps {
      // maven build src to get the jar file in target folder
        sh 'mvn -B -DskipTests clean package'
      }
    }

    stage('docker build & push image on build docker/build server') {
      agent any
      steps {
        // docker stop/rm older containers: remove only there are containers found
        script {
          def REMOVE_FLAG = sh(returnStdout: true, script: "docker container ls -aq --filter name=.*fsdms-zuul-ctn.*") != ""
          echo "REMOVE_FLAG: ${REMOVE_FLAG}"
          if(REMOVE_FLAG){
            // sh 'docker container ls -aq --format {{.ID}} --filter name=fsdms-zuul-ctn | xargs docker container rm -f'
            // sh 'docker container rm -f $(docker container ls -aq --format {{.ID}} --filter name=.*fsdms-zuul-ctn.*)'
            sh 'docker container rm -f $(docker container ls -aq --filter name=.*fsdms-zuul-ctn.*)'
          }
        }

        // docker rmi old images before build: remove only there are images found
        script {
          def REMOVE_FLAG = sh(returnStdout: true, script: "docker image ls -q *${DOCKHUB_USERNAME}/fsdms-zuul-srv*") != ""
          echo "REMOVE_FLAG: ${REMOVE_FLAG}"
          if(REMOVE_FLAG){
            // sh 'docker image ls --format {{.ID}} *${DOCKHUB_USERNAME}/fsdms-zuul-srv* | xargs docker image rm -f'
            // sh 'docker image rm -f $(docker image ls --format {{.ID}} *${DOCKHUB_USERNAME}/fsdms-zuul-srv*)'
            sh 'docker image rm -f $(docker image ls -q *${DOCKHUB_USERNAME}/fsdms-zuul-srv*)'
          }
        }

        // solution 1: can login successfully, but id/pw will be exposed
        // script {
          // docker.withRegistry(registry-server, Credentials_ID)
          // docker.withRegistry('https://index.docker.io/v1/', 'DockerHub_${DOCKHUB_USERNAME}') {
          //   def customImage = docker.build("${DOCKHUB_USERNAME}/fsdms-zuul-srv:latest", '-f Dockerfile .')
          //   /* Push the image to the docker hub Registry */
          //   customImage.push('latest')
          // }
        // }
        // soution 2: it's good to use without any expose..
        withCredentials([usernamePassword(credentialsId: "DockerHub_${DOCKHUB_USERNAME}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh 'docker login -u $USERNAME -p $PASSWORD'
          sh 'docker image build -t ${DOCKHUB_USERNAME}/fsdms-zuul-srv:latest .'
          sh 'docker push ${DOCKHUB_USERNAME}/fsdms-zuul-srv:latest'
        }
      }
    }

    // the following steps should be running on deploy server which should be different with previous server normally
    // while we use the same server is just for demo purpose
    stage('docker pull image and docker run image on docker/deploy server') {
      agent any
      steps {
        // docker stop/rm older containers: remove only there are containers found
        script {
          def REMOVE_FLAG = sh(returnStdout: true, script: "docker container ls -aq --filter name=.*fsdms-zuul-ctn.*") != ""
          echo "REMOVE_FLAG: ${REMOVE_FLAG}"
          if(REMOVE_FLAG){
            // sh 'docker container ls -aq --format {{.ID}} --filter name=fsdms-zuul-ctn | xargs docker container rm -f'
            // sh 'docker container rm -f $(docker container ls -aq --format {{.ID}} --filter name=.*fsdms-zuul-ctn.*)'
            sh 'docker container rm -f $(docker container ls -aq --filter name=.*fsdms-zuul-ctn.*)'
          }
        }

        // docker rmi old images: remove only there are images found
        script {
          def REMOVE_FLAG = sh(returnStdout: true, script: "docker image ls -q *${DOCKHUB_USERNAME}/fsdms-zuul-srv*") != ""
          echo "REMOVE_FLAG: ${REMOVE_FLAG}"
          if(REMOVE_FLAG){
            // sh 'docker image ls --format {{.ID}} *${DOCKHUB_USERNAME}/fsdms-zuul-srv* | xargs docker image rm -f'
            // sh 'docker image rm -f $(docker image ls --format {{.ID}} *${DOCKHUB_USERNAME}/fsdms-zuul-srv*)'
            // sh 'docker image rm -f $(docker image ls -q *${DOCKHUB_USERNAME}/fsdms-zuul-srv*)'
          }
        }

        // docker pull image from docker hub registry
        // sh 'docker pull ${DOCKHUB_USERNAME}/fsdms-zuul-srv'

        // docker run images
        sh 'docker run -d -p 9049:9049 -v fsdms-data:/fsdms-data --network fsdms-net --name fsdms-zuul-ctn ${DOCKHUB_USERNAME}/fsdms-zuul-srv -Xms128m -Xmx128m'
      }
    }

    stage('clean workspace') {
      agent any
      steps {
        // clean workspace after job finished
        cleanWs()
      }
    }
  }
}