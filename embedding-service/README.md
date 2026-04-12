## Running the Embedding Microservice

### **1. Create and activate a virtual environment**

```bash
python3 -m venv venv
source venv/bin/activate      # macOS / Linux
venv\Scripts\activate         # Windows
```

### **2. Install dependencies**

```bash
pip install -r requirements.txt
```

The default `requirements.txt` includes:

```
fastapi
uvicorn
numpy
sentence-transformers
```

### **3. Start the service**

```bash
uvicorn main:app --host 0.0.0.0 --port 8085
```

This will start the microservice at:

```
http://localhost:8085
```

### **4. Test the service**

You can test the embedding endpoint with:

```bash
curl -Method POST http://localhost:8085/embed -Headers @{ "Content-Type"="application/json" } -Body '{ "text": "Hello world" }'
```

You should receive a JSON array of floats.

---

## Model Notes

- The service loads the **latest trained model** automatically on startup.
- If you update the model, restart the service to use the new version.
- Embeddings are deterministic for the same model version.
