# torahAI - backend

## Basic Information

This is the backend for my torahAI streamlit chat application

Originally I built it all in python in the same proj as the UI, but I thought it would be better to be its own service


## Background

This is a RAG application - I did not train a model or anything

There is a good web service resource for receiving Jewish text (https://developers.sefaria.org/reference/getting-started)

I hit this endpoint, line by line book by book, then stored the result in a MongoDB Atlas collection

Once stored, I created a search index doc on the collection

...

## How it works

I have one POST controller/endpoint named "query"

You send your Judaism-related question, statement, text, etc. to the endpoint and...

  - based on the query in the request body, I generate embeddings (this allows for semantic search in Atlas) using openAI's embeddings endpoint
  - I take those embeddings and do a vector search in Atlas, then return the results in a list
  - loop through the list and append the semantic text that was returned
  - generate a prompt with the appended text
  - hit openAI's chat endpoint with the generated prompt
  - grab the openAI generated chat response out of the response body and return it in my "query" endpoint
  - voila!
