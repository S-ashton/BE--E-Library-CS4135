from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

app = FastAPI()

# Load a high‑quality BERT embedding model
MODEL_NAME = "sentence-transformers/all-mpnet-base-v2"
model = SentenceTransformer(MODEL_NAME)

class EmbedRequest(BaseModel):
    text: str

class EmbedResponse(BaseModel):
    embedding: list
    model_version: str

@app.get("/health")
def health():
    return {"status": "UP"}

@app.post("/embed", response_model=EmbedResponse)
def embed_text(request: EmbedRequest):
    embedding = model.encode(request.text, convert_to_numpy=True).tolist()
    return EmbedResponse(
        embedding=embedding,
        model_version=MODEL_NAME
    )
