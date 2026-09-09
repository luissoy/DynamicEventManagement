"""
===============================================================================
Microservicio de clasificacion de gravedad
===============================================================================

Carga el modelo entrenado por Entrenamiento.py y expone un unico endpoint que
devuelve la categoria de gravedad y las tres probabilidades.

Ninguno de los ocho campos de entrada es obligatorio. El modelo lleva dentro
un imputador que rellena los ausentes con la mediana del entrenamiento, de
modo que una peticion vacia tambien obtiene respuesta. Esto es deliberado: la
aplicacion de emergencia no siempre dispone de todas las constantes.
"""

import os

import joblib
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel, Field
from sklearn import set_config

# El modelo se entreno con esta opcion activa, de modo que el imputador
# devuelve un DataFrame y el bosque conserva los nombres de las columnas.
# Hay que repetirla aqui para que la inferencia sea identica al entrenamiento.
set_config(transform_output="pandas")

RUTA_MODELO = os.getenv("RUTA_MODELO", "modelo_gravedad.joblib")

_paquete = joblib.load(RUTA_MODELO)
_modelo = _paquete["modelo"]
_variables = _paquete["variables"]


class Constantes(BaseModel):
    """Los ocho campos de entrada. Ninguno es obligatorio."""

    pulse: float | None = Field(None, description="Frecuencia cardiaca (lpm)")
    popct: float | None = Field(None, description="Saturacion de oxigeno (%)")
    respr: float | None = Field(None, description="Frecuencia respiratoria (rpm)")
    tempf: float | None = Field(None, description="Temperatura (grados Fahrenheit)")
    age: float | None = Field(None, description="Edad en anos")
    sex: float | None = Field(None, description="1 mujer, 2 hombre")
    hora: float | None = Field(None, description="Hora del dia, entero de 0 a 23")
    vdayr: float | None = Field(None, description="Dia de la semana, 1 domingo a 7 sabado")


app = FastAPI(title="Clasificador de gravedad", version="1.0.0")


@app.post("/api/v1/gravedad")
def clasificar(constantes: Constantes):
    """Devuelve la categoria de gravedad y las tres probabilidades."""
    recibido = {campo.upper(): valor
                for campo, valor in constantes.model_dump().items()}
    fila = pd.DataFrame([{variable: recibido.get(variable)
                          for variable in _variables}],
                        columns=_variables).astype("float64")

    probabilidades = _modelo.predict_proba(fila)[0]

    # El orden de las columnas de predict_proba lo marca classes_, que no tiene
    # por que coincidir con el orden en que se declararon las categorias.
    # Emparejarlas a mano seria una fuente de errores silenciosos.
    por_categoria = {categoria: round(float(p), 4)
                     for categoria, p in zip(_modelo.classes_, probabilidades)}

    return {
        "gravedad": max(por_categoria, key=por_categoria.get),
        "probabilidades": por_categoria,
    }


@app.get("/api/v1/gravedad/salud")
def salud():
    """Comprobacion de que el contenedor ha levantado con el modelo cargado."""
    return {"estado": "activo", "variables": _variables}
