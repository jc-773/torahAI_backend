# torahAI - backend

## Basic Information

This is the backend for my torahAI streamlit chat application

## Background

### RAG
  - Once a valid request hits the /query endpoint 
  - Using the OpenAI embeddings endpoint, I generate embeddings (vectors) from the query received in the streamlit chat 
  - Using Mongo Atlas, I do a vector search with the generated vectors from step one. This will find similar vectors that I have stored in Atlas
  - It is important to know that by using the Sefaria API (https://developers.sefaria.org/reference/getting-started), I was able to store each line of each book as a chunck in Atlas. With each chunck having its own embedding
  - Enabling the $vectorSearch feature in Mongo Atlas, I was able to find similar embeddings to the ones generated from the chat query
  - I take the similar embeddings and build a prompt
  - I take the prompt and pass it to the OpenAI chat endpoint to generate a child friendly answer

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
