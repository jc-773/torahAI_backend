# torahAI - backend

## Basic Information

This is the backend for my torahAI streamlit chat application

## Background
  - Created by Jonathan Clark (github:jc-773)

### RAG
  - User submits a question through the Streamlit chat interface.
  - The question is sent to the /query endpoint.
  -	The app uses OpenAI’s embeddings API to convert the question into a vector (a list of numbers representing its meaning).
  -	That vector is used to perform a vector search in MongoDB Atlas, where previously stored text chunks (from Sefaria) already have their own vectors.
  -	The app finds the most similar chunks to the user’s question.
  -	These matching chunks are used to build a prompt.
  -	The prompt is sent to the OpenAI chat API to generate a child-friendly answer.

### Other things about the backend
  - The chat requests/responses are low latency and non-blocking, which is great. I was able to achieve a less than 2000 millisecond response by using Spring's Project Reactor event driven architecture (and a singleton virtual thread)
  - This basically creates a chain of non-blocking reactive events that execute in the order of the RAG section explained above

## How it works

(You will need an OpenAI API key)
(As of now... /query endpoint is a POST method that requires a body with the following structure:
  {
    "model": "gpt-3.5-turbo",
    "input": "book_of_genesis_related_question",
    "encoding_format": "float"
  })

  - I will have a swagger doc very soon but...
  - There are two endpoints 1. /query 2. /query/image
  - It is recommended for clients to use the query and query image endpoints sequentially by passing the same (or related) prompt to generate images related to the query itself
  - As of v1.0, the AI should be an expert on creating kid-friendly responses related to queries about the book of genesis


## Continuous Integration
  - Right now, when a change is made to master, I have a YAML job that kicks off with the following steps:
      - checkout the repo
      - downloads jdk 23
      - run the unit tests using the  maven wrapper (quality gate)
      - if the tests pass, build the project with maven wrapper (quality gate)
      - sets up AWS credentials
      - logs into AWS ECR
      - sets the image URI with ECR repo and ECR registry
      - builds the app into a docker image pointing at image URI at "." (root directory)
      - run docker push to deploy the image to AWS ECR tagged with "latest"
