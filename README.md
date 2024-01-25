<img src="https://github.com/Ninja-Cyborg/spring_react_application/assets/66517017/6a85363f-2258-4b59-a468-e3d3ea6ef1a6" width="200"/>

# KaleidoHub
A Kaleidoscopic Web Experience.
 > *Scroll to the bottom for a fun-fact*

# Description
-  Modern, easy to use, feature-rich and secure **Full Stack** CRM and File Sharing web application.

# Key Features
- Seamless Chating
- JWT authentication
- File Sharing

# Goal
- Implement HTTPS to encrypt data transmission between the client and server
- Utilize role-based access control(RBAC) to manage permission
- Employ secure authentication mechanisms, JWT to provide authorized users access
- Embrace a Decoupled architecture with Java backend, React in the frontend, and a RESTful API ensuring independence between the server and client components.
- Implemented a streamlined CI/CD pipeline using GitHub Actions to automate testing, build, and deployment workflows
- Promoting code quality, faster delivery cycles, security management
- Uses AWS S3 service for file download and upload based on user access
- Leverage Docker to containerize applications, ensuring consistency across different platforms

# Story behind the application
- I'd developed Java backend applications in the past while implementing Spring, Spring Security. I want to create an application with a rich user interface, improved features, better security, and an easy management. This Awesome **FullStack Application** is the product of that. With a lot more features to come.

# Application Stack
-  Frontend: React
-  Backend: Java Spring & Spring Security
-  Database: PostgreSQL, Flyway(Database version/migration)
-  File Storage: AWS S3
-  CI/CD tool: GitHub Actions, GitHub, Slack, Docker

# Getting Started
- This assumes that you already have a working environment to run Java applications, npm, PostgreSQL, and Docker. 
## Installing App Locally
- Download the zip file for this repository
- Unzip and open the directory in IDE
## Run with Docker
### Open directory in the terminal containing where docker-compose.yml is located
- Run `docker login` to make sure you're logged in
- Run 'docker-compose up'
- It will pull and run containers for the backend, frontend, and database as specified in yml file
- The credentials for the postgres container can be modified from yml file
## Run manually
### 1. Create member Table in the Database
-  Execute the [V1__Initial_Setup.sql](https://github.com/Ninja-Cyborg/spring_react_application/blob/main/backend/src/main/resources/db/migration/V1__Initial_Setup.sql) script in postgres
### 2. Running backend  
-  Open and run Main.java in backend/src/main/java/com/backend/Main.java
### 3. Running frontend
-  Open the terminal in react-ui directory and run `npm run dev`
-  react-ui is located under frontend/
### Access the application at 'http://localhost:5173/signup' in the web browser
## Using containers from Dockerhub
## Run on Cloud(AWS)
### Setup database
- Create an RDS database on AWS with the same version as used for the application(Postgres 15.3 at current version)
- ssh to database through EC2 instance
- install postgres in EC2
- setup TABLE MEMBER
### Running backend and frontend
- Add the URL to RDS database in Dockerrun.aws.json at "jdbc:postgresql://{RDS-URL}:5432/member"
- Use Dockerrun.aws.json file to deploy frontend and backend on AWS ElasticBeanStalk
- You can access the deployed application on the ip-address on EBS

# Documentation
-  *NOTICE* Documentation for the security config, access control, and security group setup on aws is unfinished

# Contributing
- First, open an issue describing the bug or enhancement for discussion.
- Create a Pull Request with your changes against the master branch.
- Please feel free to reach out. I will be amused. :smiley:

## Some Screen Captures:
- List Members:
- ![image](https://github.com/Ninja-Cyborg/spring_react_application/assets/66517017/e9d34dbb-5cb3-44fc-941b-74ae030f95f0)

# FunFact
"[Kaleidoscopic](https://www.merriam-webster.com/dictionary/kaleidoscopic)" means changing one thing to another

## Why the name **KaleidoHub**?
This is one of the most robust applications I have developed. I plan to add more features and expand it while also learning to implement and integrate different technologies. Its decoupled architecture allows one to make quick changes and it has immense potential for expansion, from adding more features to integrating it as an in-built chat in a game or using a completely different language in a component. Featuring a single version with the versatility for multi-expansions, embodying a kaleidoscopic nature of possibilities.
