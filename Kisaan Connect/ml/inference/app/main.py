from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class InferReq(BaseModel):
    image_url: str | None = None

@app.get("/health")
def health():
    return {"status":"UP"}

@app.post("/infer")
def infer(req: InferReq):
    return {
        "model_version": "stub-0.1",
        "labels": [
            {"label":"aphids","confidence":0.82},
            {"label":"leaf_spot","confidence":0.13},
            {"label":"healthy","confidence":0.05}
        ]
    }

