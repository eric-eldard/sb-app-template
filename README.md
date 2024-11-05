# Spring Boot Template App
A runnable template for a Spring Boot app, based on [eric-eldard/portfolio](https://github.com/eric-eldard/portfolio)

## Features

### Deployment
- Fat jar
- TypeScript compilation and bundling for use in browser
- Deploy and run scripts
- Spring Boot Actuator access for admins

### Users
- Basic user entity
- Login attempt records
- Page view logging
- Admin role
- Support for Grant Authorities
- Robust user management for admins
- DDL for user and permission tables

### AuthN & AuthZ
- Stateless JWS auth tokens
- Tracking of stale JWTs
- Custom CSRF (supporting session-less auth)

### UI
- Splash/home page
- Login page
- Main page with popup widget
- Error page
- Media tagging CSS library
- Full user management UI

### Tests
- Unit and integration tests for all of these features