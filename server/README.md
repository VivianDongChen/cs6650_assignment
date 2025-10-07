# Server Module

This module will host the Assignment 1 WebSocket server implementation.

## Planned Contents
- Maven-based Java project (WAR packaging)
- `/chat/{roomId}` Jakarta WebSocket endpoint with validation and echo logic
- `/health` REST endpoint for status checks
- Deployment scripts/instructions for Tomcat on AWS EC2 (us-west-2)

## Local Development TODO
1. Initialize Maven archetype and configure dependencies.
2. Implement model, validator, and handler classes.
3. Add integration tests using `wscat` or equivalent.
4. Document build and run commands in this file as the project takes shape.
