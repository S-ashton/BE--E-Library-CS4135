"""Placeholder embedding service"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/health")
def health():
    return {"status": "UP", "placeholder": True}