# =============================================================================
# EJECUCION EN GOOGLE COLAB
# -----------------------------------------------------------------------------
# Pega este fichero entero en una sola celda y dale a ejecutar. No hay que
# tocar nada ni subir ningun archivo: los datos se descargan solos del CDC.
#
# Al terminar imprime un BLOQUE PARA COPIAR con todas las cifras y lanza la
# descarga de salida_modelo.zip, que lleva dentro el modelo para el
# microservicio, las metricas en JSON y las figuras.
# =============================================================================

import subprocess
import sys

for _paquete, _modulo in [("pandas", "pandas"), ("scikit-learn", "sklearn"),
                          ("matplotlib", "matplotlib"), ("joblib", "joblib"),
                          ("lightgbm", "lightgbm")]:
    try:
        __import__(_modulo)
    except ImportError:
        print(f"Instalando {_paquete}...")
        subprocess.run([sys.executable, "-m", "pip", "install", "-q", _paquete],
                       check=False)

"""
===============================================================================
TFM - Modulo de clasificacion de gravedad
Entrenamiento del clasificador sobre NHAMCS
===============================================================================

Luis Serrano Marin - Master Universitario en Inteligencia Artificial (UNIR)
"Diseno e implementacion de una arquitectura dinamica de gestion de eventos
 con integracion modular de Inteligencia Artificial"

QUE HACE ESTE SCRIPT
--------------------
Descarga el fichero de urgencias de NHAMCS, prepara las ocho variables de
entrada, agrupa el nivel de triaje en tres categorias de gravedad, entrena
cinco modelos, los compara y guarda el modelo ganador en un fichero.

Ese fichero es lo unico que el microservicio necesita. Este script se ejecuta
UNA VEZ y fuera del sistema: no forma parte del despliegue.

COMO EJECUTARLO
---------------
En Google Colab:  se pega entero en una celda y se ejecuta. Nada mas.
En local:         pip install pandas scikit-learn joblib matplotlib

Tiempo estimado: menos de dos minutos, casi todo de descarga.

QUE IMPRIME AL TERMINAR
-----------------------
Un bloque rotulado "RESUMEN PARA LA MEMORIA" con todas las cifras que hay que
llevar al TFM, cada una con su etiqueta. Ninguna cifra de este script esta
escrita de antemano: todas salen de la ejecucion.

DECISIONES QUE ESTE CODIGO IMPLEMENTA (archivo B de la sesion S-Datos)
---------------------------------------------------------------------
D-1  Un solo ano, 2022, ultimo de la serie
D-2  No se emplea el peso muestral
D-3  Ocho variables: cuatro de sensor, dos de perfil, dos de reloj
D-4  Se descarta la tension arterial
D-8  Tres categorias de gravedad: ALTA, MEDIA, BAJA
D-12 Se entrenan cinco modelos y se comparan
D-13 Particion 70-30 estratificada con semilla fija
D-14 Ponderacion de clases para mover la sensibilidad de ALTA
===============================================================================
"""

import io
import json
import zipfile
import urllib.request
from pathlib import Path

import numpy as np
import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import StandardScaler
from sklearn.dummy import DummyClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier, HistGradientBoostingClassifier
from sklearn.metrics import (
    classification_report, confusion_matrix, accuracy_score,
    recall_score, precision_score, roc_auc_score,
)
import joblib
import warnings
import matplotlib
matplotlib.use("Agg")               # sin ventana grafica, solo ficheros
import matplotlib.pyplot as plt
from sklearn import set_config

# Los transformadores devuelven DataFrame en lugar de matriz, de modo que los
# modelos conservan los nombres de las variables. Esto quita el aviso de
# LightGBM sobre nombres de columna que aparecia en la ejecucion anterior.
set_config(transform_output="pandas")
warnings.filterwarnings("ignore", category=UserWarning)
warnings.filterwarnings("ignore", category=FutureWarning)


# =============================================================================
# 1. CONFIGURACION
#    Todo lo ajustable esta aqui. No hace falta tocar nada mas abajo.
# =============================================================================

# --- Ano del fichero. Es una lista para poder agregar mas anos si la clase
#     minoritaria resultase escasa (contingencia declarada en el apartado 1.3).
#     El formato de 2021 y 2022 es identico, asi que anadir 2021 funciona sin
#     mas cambios. Con 2022 solo deberia bastar.
#     Se ejecutan LOS DOS escenarios en una sola pasada y se comparan al
#     final. El fichero de 2022 se descarga una sola vez y se reutiliza.
CONFIGURACIONES = {
    "Solo 2022":      [2022],
    "Cuatro anos":    [2018, 2019, 2021, 2022],
}
#     2020 se deja fuera a proposito: es el ano mas distorsionado por la
#     pandemia y su composicion de casos no es comparable con el resto.

# --- Semilla. Se documenta en el Anexo D: sin ella el resultado no es
#     reproducible y la memoria no se puede verificar.
SEMILLA = 42

# --- Proporcion de la particion de prueba. 30 %, igual que Raita et al. (2019).
PROPORCION_PRUEBA = 0.30

# --- Las ocho variables de entrada.
#     Para anadir una variable basta con incluirla aqui y, si necesita alguna
#     conversion, tratarla en la funcion limpiar(). Nada mas del script cambia.
VARIABLES = [
    "PULSE",    # sensor - frecuencia cardiaca (lpm)
    "POPCT",    # sensor - saturacion de oxigeno (%)
    "RESPR",    # sensor - frecuencia respiratoria (rpm)
    "TEMPF",    # sensor - temperatura (grados Fahrenheit, decimal implicito)
    "AGE",      # perfil - edad en anos (94 = 94 o mas)
    "SEX",      # perfil - 1 mujer, 2 hombre
    "HORA",     # reloj  - derivada de ARRTIME, entero de 0 a 23
    "VDAYR",    # reloj  - dia de la semana, 1 domingo a 7 sabado
]

# --- Columnas que hay que leer del fichero (las de VARIABLES mas las de origen)
#     BPSYS y BPDIAS se leen aunque esten descartadas del modelo, porque se
#     usan en la ablacion del apartado 5d para medir cuanto cuesta el criterio
#     de sensor. No entran nunca en el modelo principal.
COLUMNAS_ORIGEN = ["PULSE", "POPCT", "RESPR", "TEMPF", "AGE", "SEX",
                   "ARRTIME", "VDAYR", "IMMEDR", "BPSYS", "BPDIAS"]

# --- Cortes binarios que se van a comparar en el apartado 5c.
#     La clave es la etiqueta que se imprime; el valor, los niveles que quedan
#     en el grupo "grave". El objetivo es ver cual da un modelo mas util, no
#     cual da una exactitud mas vistosa.
CORTES_BINARIOS = {
    "1 | 2345": [1],
    "12 | 345": [1, 2],
    "123 | 45": [1, 2, 3],
    "1234 | 5": [1, 2, 3, 4],
}

# --- Agrupacion de los cinco niveles de triaje en tres categorias.
#     Justificada en el apartado 3.4 del archivo B: la unica frontera de la
#     escala que responde a la urgencia es la de los niveles 1 y 2 frente al
#     resto (Emergency Nurses Association, 2023).
AGRUPACION = {1: "ALTA", 2: "ALTA", 3: "MEDIA", 4: "BAJA", 5: "BAJA"}
CATEGORIAS = ["ALTA", "MEDIA", "BAJA"]
CATEGORIA_CRITICA = "ALTA"   # sobre la que se mide la sensibilidad

# --- Umbrales de la regla de referencia (constantes en zona de riesgo).
#     R-4 CERRADO. Los tres valores de adulto estan verificados contra el
#     manual de la escala. Si prefieres no defender umbrales clinicos, pon
#     USAR_REGLA_CLINICA = False y la regla desaparece de la comparacion sin
#     que nada mas se rompa.
USAR_REGLA_CLINICA = True

# --- Estratificacion por edad de la regla.
#
#     Lo que faltaba no eran los valores sino la franja a la que se aplican.
#     El conjunto de datos NO esta filtrado por edad, de modo que aplicar el
#     umbral de adulto a un registro pediatrico lo marca como grave por
#     construccion y no por gravedad: en un nino pequeno el pulso y la
#     frecuencia respiratoria normales ya superan los cortes del adulto.
#
#     Eso hunde la precision de la regla de forma artificial e infla la
#     ventaja del modelo, que si dispone de la edad entre sus variables.
#
#     Con False se recupera el comportamiento anterior, umbrales de adulto
#     para todos los registros. Conviene ejecutar las dos veces: la
#     diferencia entre ambas cifras es el tamano del sesgo, y esa diferencia
#     es una frase para el capitulo 7 sin escribir una linea mas.
REGLA_POR_FRANJAS = True

# --- Umbrales de adulto. Se aplican con REGLA_POR_FRANJAS a False, y como
#     respaldo a los registros cuya edad esta ausente.
UMBRAL_PULSO = 100      # lpm, por encima -> riesgo
UMBRAL_RESPIRATORIA = 20  # rpm, por encima -> riesgo
UMBRAL_SATURACION = 92    # %, por debajo -> riesgo

# --- Tabla de umbrales por franja de edad.
#
#     R-4 CERRADO CON FUENTE. Estos valores son los del punto de decision D
#     del algoritmo ESI, version 5, tal como aparece en el manual de la escala
#     (Emergency Nurses Association, 2023). Es la misma referencia que ya cita
#     el apartado 5.10.7 de la memoria, de modo que no hay que anadir
#     bibliografia nueva.
#
#     Tabla del manual, siete franjas:
#         < 1 mes     pulso > 190   respiratoria > 60
#         1-12 meses  pulso > 180   respiratoria > 55
#         1-3 anos    pulso > 140   respiratoria > 40
#         3-5 anos    pulso > 120   respiratoria > 35
#         5-12 anos   pulso > 120   respiratoria > 30
#         12-18 anos  pulso > 100   respiratoria > 20
#         > 18 anos   pulso > 100   respiratoria > 20
#     Saturacion < 92 % en todas las franjas, por eso no aparece en la tabla.
#
#     ADAPTACION DECLARADA: la variable AGE del fichero viene en anos enteros,
#     de modo que las dos primeras franjas del manual no se pueden separar.
#     Se colapsan en una sola fila de menores de un ano y se le aplica la de
#     1-12 meses, que cubre once de los doce meses del tramo. Quedan seis
#     filas en lugar de siete. Hay que declararlo en la memoria en una frase.
#
#     Cada fila es (edad_maxima_exclusiva, umbral_pulso, umbral_respiratoria).
#     La ultima cierra con float("inf") y coincide con los umbrales de adulto.
UMBRALES_POR_EDAD = [
    (1,            180, 55),   # menores de 1 ano (franjas 1 y 2 del manual)
    (3,            140, 40),   # 1-2 anos
    (5,            120, 35),   # 3-4 anos
    (12,           120, 30),   # 5-11 anos
    (18,           100, 20),   # 12-17 anos
    (float("inf"), UMBRAL_PULSO, UMBRAL_RESPIRATORIA),   # 18 anos o mas
]

# --- Salidas
DIR_SALIDA = Path("salida_modelo")
URL_PLANTILLA = ("https://ftp.cdc.gov/pub/health_statistics/nchs/"
                 "dataset_documentation/NHAMCS/stata/ed{anio}-stata.zip")


# =============================================================================
# 2. DESCARGA Y LECTURA
# =============================================================================

_CACHE = {}


def descargar_y_leer(anio):
    """
    Descarga el fichero de un ano y lo devuelve como DataFrame.

    Se usa la version ya montada para Stata en lugar del ASCII de ancho fijo:
    trae los nombres de las variables puestos y evita el libro de codigos
    posicional. El zip contiene un unico .dta, cuyo nombre exacto puede variar,
    asi que se busca en lugar de darlo por supuesto.

    convert_categoricals=False es IMPORTANTE: sin el, las variables con
    etiquetas de valor se leen como texto y IMMEDR llegaria como
    "Immediate"/"Emergent" en vez de como 1/2.
    """
    if anio in _CACHE:
        print(f"  Ano {anio}: reutilizando la descarga anterior")
        return _CACHE[anio].copy()

    url = URL_PLANTILLA.format(anio=anio)
    print(f"  Descargando {url} ...")
    with urllib.request.urlopen(url) as respuesta:
        contenido = respuesta.read()
    print(f"  Descargados {len(contenido):,} bytes")

    with zipfile.ZipFile(io.BytesIO(contenido)) as z:
        nombres_dta = [n for n in z.namelist() if n.lower().endswith(".dta")]
        if not nombres_dta:
            raise RuntimeError(f"El zip de {anio} no contiene ningun .dta: "
                               f"{z.namelist()}")
        print(f"  Fichero encontrado dentro del zip: {nombres_dta[0]}")
        with z.open(nombres_dta[0]) as f:
            df = pd.read_stata(io.BytesIO(f.read()), convert_categoricals=False)

    # Los nombres pueden venir en minusculas segun el ano. Se normalizan.
    df.columns = [c.upper() for c in df.columns]
    # assign + copy en lugar de df["ANIO"] = anio, para evitar el aviso de
    # fragmentacion que da pandas al insertar una columna en un marco ancho.
    df = df.assign(ANIO=anio).copy()
    _CACHE[anio] = df
    return df.copy()


def cargar_datos(anios):
    marcos = []
    for anio in anios:
        print(f"\nAno {anio}")
        marcos.append(descargar_y_leer(anio))
    df = pd.concat(marcos, ignore_index=True)

    faltan = [c for c in COLUMNAS_ORIGEN if c not in df.columns]
    if faltan:
        raise RuntimeError(
            f"No estan en el fichero estas columnas: {faltan}. "
            f"Revisa la documentacion del ano correspondiente."
        )
    return df


# =============================================================================
# 3. LIMPIEZA
#    Aqui viven las dos trampas del fichero: los codigos negativos y los
#    codigos centinela. Si no se tratan, el modelo entrena sobre disparates.
# =============================================================================

def limpiar(df):
    """
    Convierte codigos de no respuesta y centinelas a ausente, y deja cada
    variable en su unidad real.

    Convenio del NCHS:
      -9 = campo en blanco
      -8 = desconocido
      -7 = no aplicable
    Ademas:
      PULSE  = 998 significa "detectado por Doppler", no 998 pulsaciones
      TEMPF  viene con decimal implicito: 0986 son 98,6 grados Fahrenheit
    """
    d = df.copy()

    # Todo valor negativo es un codigo de no respuesta en este fichero.
    for col in COLUMNAS_ORIGEN:
        d[col] = pd.to_numeric(d[col], errors="coerce")
        d.loc[d[col] < 0, col] = np.nan

    # Codigo centinela de la frecuencia cardiaca.
    d.loc[d["PULSE"] == 998, "PULSE"] = np.nan
    # Codigo centinela de la tension diastolica (palpacion o Doppler).
    d.loc[d["BPDIAS"] == 998, "BPDIAS"] = np.nan

    # Decimal implicito de la temperatura.
    d["TEMPF"] = d["TEMPF"] / 10.0

    # Rangos plausibles segun el libro de codigos. Lo que caiga fuera es error.
    d.loc[~d["PULSE"].between(0, 240), "PULSE"] = np.nan
    d.loc[~d["RESPR"].between(0, 150), "RESPR"] = np.nan
    d.loc[~d["POPCT"].between(0, 100), "POPCT"] = np.nan
    d.loc[~d["TEMPF"].between(89.6, 105.6), "TEMPF"] = np.nan
    d.loc[~d["BPSYS"].between(43, 289), "BPSYS"] = np.nan
    d.loc[~d["BPDIAS"].between(22, 190), "BPDIAS"] = np.nan

    # Hora del dia a partir de la hora de llegada en formato militar.
    # Se usa la hora entera y no una codificacion ciclica: los modelos de
    # arboles parten por umbrales sobre cada eje y no necesitan continuidad en
    # el extremo, y ademas "las 3 de la madrugada" es una explicacion legible.
    d["HORA"] = (d["ARRTIME"] // 100)
    d.loc[~d["HORA"].between(0, 23), "HORA"] = np.nan

    # Sexo a 0 y 1 para que no se lea como una magnitud ordenada.
    d["SEX"] = d["SEX"].map({1: 0, 2: 1})

    return d


def construir_etiqueta(df):
    """
    Se queda solo con los registros que tienen un nivel de triaje real.

    Se descartan:
      -9 y -8  ausente y desconocido (27,8 % del fichero, sin imputar)
      0        se consigno "sin triaje" aunque el area si triaja
      7        el area de servicio no realiza triaje de enfermeria

    Una etiqueta ausente no se puede imputar sin sustituir el objetivo del
    aprendizaje, de modo que esas filas se caen. Es el motivo principal de la
    merma entre los registros iniciales y los finales.
    """
    d = df[df["IMMEDR"].isin([1, 2, 3, 4, 5])].copy()
    d["NIVEL"] = d["IMMEDR"].astype(int)
    d["GRAVEDAD"] = d["NIVEL"].map(AGRUPACION)
    return d


# =============================================================================
# 4. MODELO DE REFERENCIA POR REGLAS
#    Reproduce con datos propios el contraste que sostiene todo el capitulo 2:
#    un modelo entrenado frente a un conjunto de reglas fijadas de antemano.
# =============================================================================

def umbrales_de_cada_registro(X, por_franjas=None):
    """
    Devuelve el umbral de pulso y el de frecuencia respiratoria que le toca a
    cada fila segun la franja de edad en la que cae.

    Los registros sin edad conservan los umbrales de adulto. Es el respaldo
    mas conservador: no inventa una franja y deja el comportamiento anterior.

    por_franjas permite forzar una variante concreta sin tocar la constante
    global, que es lo que hace falta para medir las dos en una sola ejecucion.
    """
    if por_franjas is None:
        por_franjas = REGLA_POR_FRANJAS

    u_pulso = pd.Series(float(UMBRAL_PULSO), index=X.index)
    u_resp = pd.Series(float(UMBRAL_RESPIRATORIA), index=X.index)

    if not por_franjas:
        return u_pulso, u_resp

    anterior = -1.0
    for edad_max, up, ur in UMBRALES_POR_EDAD:
        en_franja = ((X["AGE"] >= anterior) & (X["AGE"] < edad_max)).fillna(False)
        u_pulso.loc[en_franja] = float(up)
        u_resp.loc[en_franja] = float(ur)
        anterior = edad_max

    return u_pulso, u_resp


def regla_clinica(X, por_franjas=None):
    """Devuelve ALTA si alguna constante esta en zona de riesgo."""
    u_pulso, u_resp = umbrales_de_cada_registro(X, por_franjas)
    riesgo = (
        (X["PULSE"] > u_pulso)
        | (X["RESPR"] > u_resp)
        | (X["POPCT"] < UMBRAL_SATURACION)
    )
    # Los ausentes cuentan como sin riesgo: una regla fija no sabe imputar,
    # y esa es precisamente una de sus limitaciones frente al modelo.
    return np.where(riesgo.fillna(False), "ALTA", "MEDIA")


def reparto_por_franjas(X):
    """
    Cuantos registros caen en cada franja y cuantos se quedan sin edad.

    Sirve para poder escribir en la memoria que proporcion del conjunto recibe
    umbrales distintos a los del adulto, que es la magnitud que justifica todo
    este apartado.
    """
    filas = []
    anterior = -1.0
    for edad_max, up, ur in UMBRALES_POR_EDAD:
        en_franja = ((X["AGE"] >= anterior) & (X["AGE"] < edad_max)).fillna(False)
        inicio = int(max(anterior, 0))
        if edad_max == float("inf"):
            etiqueta = f"{inicio} anos o mas"
        elif edad_max - inicio == 1:
            etiqueta = f"{inicio} anos"
        else:
            etiqueta = f"{inicio}-{int(edad_max) - 1} anos"
        filas.append({"franja": etiqueta, "pulso": up, "respiratoria": ur,
                      "n": int(en_franja.sum())})
        anterior = edad_max

    sin_edad = int(X["AGE"].isna().sum())
    no_adultos = sum(f["n"] for f in filas[:-1])
    return filas, sin_edad, no_adultos


# =============================================================================
# 5. PROGRAMA PRINCIPAL
# =============================================================================

def auc_con_intervalo(y_bin, proba, repeticiones=1000):
    """
    Area bajo la curva con intervalo de confianza al 95 % por remuestreo.

    Un AUC sin intervalo es un numero suelto; con el se puede afirmar que el
    modelo esta por encima del azar y no solo que lo parece. Es barato de
    calcular y da bastante solidez a la cifra en la memoria.
    """
    rng = np.random.default_rng(SEMILLA)
    y = np.asarray(y_bin)
    p = np.asarray(proba)
    valores = []
    for _ in range(repeticiones):
        i = rng.integers(0, len(y), len(y))
        if len(np.unique(y[i])) < 2:
            continue
        valores.append(roc_auc_score(y[i], p[i]))
    bajo, alto = np.percentile(valores, [2.5, 97.5])
    return round(float(np.mean(valores)), 4), round(float(bajo), 4), round(float(alto), 4)


def concentracion_de_riesgo(y_bin, proba):
    """
    Cuanto concentra el modelo los casos graves en lo alto de su ranking.

    Es la forma mas intuitiva de expresar la utilidad de un modulo que sirve
    para PRIORIZAR avisos, que es exactamente su funcion en el sistema. Dice
    algo que cualquiera entiende sin saber que es un area bajo la curva: si se
    ordenan los eventos por gravedad estimada, que proporcion de los graves
    reales cae en el primer decil, y cuantas veces supera eso a la tasa base.
    """
    y = np.asarray(y_bin)
    orden = np.argsort(-np.asarray(proba))
    y_ord = y[orden]
    base = y.mean()
    filas = []
    for pct in [0.10, 0.20, 0.30, 0.50]:
        k = max(1, int(len(y) * pct))
        tasa = y_ord[:k].mean()
        filas.append({
            "fraccion_revisada": pct,
            "tasa_en_el_grupo": round(float(tasa), 4),
            "tasa_base": round(float(base), 4),
            "concentracion": round(float(tasa / base), 2) if base else None,
            "graves_capturados": round(float(y_ord[:k].sum() / y.sum()), 4),
        })
    return filas


# =============================================================================
# FIGURAS PARA LA MEMORIA
# Se guardan en PNG a 200 puntos por pulgada, listas para insertar en el
# capitulo 6. Una figura se lee mejor que una captura de consola y es lo que
# se espera en un apartado de resultados.
# =============================================================================

ESTILO = {"figure.dpi": 120, "savefig.dpi": 200, "font.size": 9,
          "axes.grid": True, "grid.alpha": 0.3, "axes.spines.top": False,
          "axes.spines.right": False, "savefig.bbox": "tight"}


def _guardar(fig, nombre, sufijo, indice):
    ruta = DIR_SALIDA / f"fig{indice}_{nombre}_{sufijo}.png"
    fig.savefig(ruta)
    plt.close(fig)
    print(f"    Figura guardada: {ruta.name}")
    return ruta


def generar_figuras(datos, resumen, mc, importancias, sufijo):
    """Produce las seis figuras del capitulo 6."""
    print("\n  GENERANDO FIGURAS")
    plt.rcParams.update(ESTILO)
    rutas = []

    # --- Figura 1: reparto de clases -------------------------------------
    fig, (a1, a2) = plt.subplots(1, 2, figsize=(9, 3.2))
    d5 = datos["NIVEL"].value_counts().sort_index()
    et5 = ["1 Inmediato", "2 Emergente", "3 Urgente", "4 Semiurgente", "5 No urgente"]
    a1.bar([et5[i - 1] for i in d5.index], d5.values, color="#4a6fa5")
    a1.set_title("Niveles de triaje del conjunto")
    a1.tick_params(axis="x", rotation=35)
    a1.set_ylabel("Registros")
    d3 = datos["GRAVEDAD"].value_counts().reindex(CATEGORIAS)
    a2.bar(CATEGORIAS, d3.values, color=["#b5484c", "#c9a227", "#4c8b64"])
    a2.set_title("Categorias de gravedad agrupadas")
    for i, v in enumerate(d3.values):
        a2.text(i, v, f"{100*v/len(datos):.1f} %", ha="center", va="bottom")
    rutas.append(_guardar(fig, "reparto_clases", sufijo, 1))

    # --- Figura 2: comparacion de familias -------------------------------
    fam = [f for f in resumen.get("comparacion_familias", [])
           if f["auc"] is not None and f["modelo"] != "Trivial"]
    if fam:
        fam = sorted(fam, key=lambda f: f["auc"])
        fig, ax = plt.subplots(figsize=(7.5, 3.6))
        y = np.arange(len(fam))
        err = [[f["auc"] - f["ic_bajo"] for f in fam],
               [f["ic_alto"] - f["auc"] for f in fam]]
        colores = {"Refuerzo": "#4a6fa5", "Agregacion": "#3f7d6a",
                   "Lineal": "#c9a227", "Red neuronal": "#a05195"}
        ax.barh(y, [f["auc"] for f in fam], xerr=err, capsize=3,
                color=[colores.get(f["familia"], "#888") for f in fam])
        ax.set_yticks(y, [f["modelo"] for f in fam])
        ax.axvline(0.5, color="#b5484c", ls="--", lw=1)
        ax.text(0.503, -0.6, "azar", color="#b5484c", fontsize=8)
        ax.set_xlim(0.45, 0.75)
        ax.set_xlabel("Area bajo la curva, con intervalo de confianza al 95 %")
        ax.set_title("Siete modelos de cuatro familias sobre el mismo problema")
        rutas.append(_guardar(fig, "familias_modelos", sufijo, 2))

    # --- Figura 3: curva ROC con el punto de la regla --------------------
    roc = resumen.get("_roc", None)
    if roc:
        fig, ax = plt.subplots(figsize=(4.6, 4.4))
        ax.plot(roc["fpr"], roc["tpr"], lw=2, color="#4a6fa5",
                label=f"Modulo (AUC {roc['auc']:.3f})")
        ax.plot([0, 1], [0, 1], ls="--", lw=1, color="#999", label="Azar")
        cp = resumen.get("comparacion_pareada")
        if cp and "especificidad" in cp["regla"]:
            # Punto de operacion REAL de la regla: su propia sensibilidad y su
            # propia especificidad. Si queda a la derecha de la curva, quiere
            # decir que el modulo alcanza esa misma sensibilidad equivocandose
            # menos, que es el contraste que se quiere mostrar.
            sr = cp["regla"]["sensibilidad"]
            er = cp["regla"]["especificidad"]
            em = cp["modelo_mismo_punto"]["especificidad"]
            ax.plot(1 - er, sr, "D", ms=8, color="#b5484c", zorder=5,
                    label="Regla sobre constantes")
            if em > er:
                ax.annotate("a igual sensibilidad,\nel modulo se equivoca menos",
                            xy=(1 - er, sr), xytext=(min(0.97, 1 - er + 0.10),
                                                     max(0.05, sr - 0.28)),
                            fontsize=8, color="#b5484c", ha="left",
                            arrowprops=dict(arrowstyle="->", color="#b5484c", lw=1))
        ax.set_xlabel("1 - especificidad")
        ax.set_ylabel("Sensibilidad")
        ax.set_title("Discriminacion de la categoria ALTA")
        ax.legend(loc="lower right", fontsize=8)
        rutas.append(_guardar(fig, "curva_roc", sufijo, 3))

    # --- Figura 4: matriz de confusion -----------------------------------
    fig, ax = plt.subplots(figsize=(4.4, 3.8))
    norm = mc / mc.sum(axis=1, keepdims=True)
    im = ax.imshow(norm, cmap="Blues", vmin=0, vmax=norm.max())
    ax.set_xticks(range(3), CATEGORIAS)
    ax.set_yticks(range(3), CATEGORIAS)
    ax.set_xlabel("Categoria predicha")
    ax.set_ylabel("Categoria real")
    ax.set_title("Matriz de confusion")
    ax.grid(False)
    for i in range(3):
        for j in range(3):
            ax.text(j, i, f"{mc[i, j]:,}\n{100*norm[i, j]:.1f} %",
                    ha="center", va="center", fontsize=8,
                    color="white" if norm[i, j] > norm.max() * 0.6 else "black")
    fig.colorbar(im, ax=ax, fraction=0.046, label="Proporcion de la fila")
    rutas.append(_guardar(fig, "matriz_confusion", sufijo, 4))

    # --- Figura 5: importancia de variables ------------------------------
    fig, ax = plt.subplots(figsize=(5.5, 3.2))
    imp = importancias.sort_values()
    origen = {"PULSE": "#4a6fa5", "POPCT": "#4a6fa5", "RESPR": "#4a6fa5",
              "TEMPF": "#4a6fa5", "AGE": "#3f7d6a", "SEX": "#3f7d6a",
              "HORA": "#c9a227", "VDAYR": "#c9a227"}
    ax.barh(imp.index, imp.values, color=[origen.get(v, "#888") for v in imp.index])
    ax.set_xlabel("Importancia relativa")
    ax.set_title("Aportacion de cada variable")
    from matplotlib.patches import Patch
    ax.legend(handles=[Patch(color="#4a6fa5", label="Sensor"),
                       Patch(color="#3f7d6a", label="Perfil"),
                       Patch(color="#c9a227", label="Reloj")],
              fontsize=8, loc="lower right")
    rutas.append(_guardar(fig, "importancia_variables", sufijo, 5))

    # --- Figura 6: concentracion de riesgo -------------------------------
    conc = resumen.get("concentracion_riesgo")
    if conc:
        fig, ax = plt.subplots(figsize=(5.2, 3.4))
        x = [0] + [100 * f["fraccion_revisada"] for f in conc] + [100]
        yv = [0] + [100 * f["graves_capturados"] for f in conc] + [100]
        ax.plot(x, yv, "o-", color="#4a6fa5", label="Modulo")
        ax.plot([0, 100], [0, 100], "--", color="#999", label="Orden aleatorio")
        ax.set_xlabel("Porcentaje de eventos revisados, ordenados por gravedad estimada")
        ax.set_ylabel("Porcentaje de eventos graves capturados")
        ax.set_title("Concentracion de los casos graves")
        ax.legend(fontsize=8)
        rutas.append(_guardar(fig, "concentracion_riesgo", sufijo, 6))

    print(f"    {len(rutas)} figuras en {DIR_SALIDA}/")
    return rutas


def comparar_familias(datos, resumen):
    """
    ===========================================================================
    4b. SIETE FAMILIAS DE MODELOS SOBRE EL MISMO PROBLEMA
    ===========================================================================

    Este apartado responde a una pregunta que el trabajo daba por contestada
    sin comprobarla: por que un modelo basado en arboles.

    La justificacion del capitulo 5 no dice que los arboles sean superiores.
    Dice que son la familia mas empleada en el dominio de la clasificacion de
    alarmas (Chromik et al., 2022) y que encajan con el tamano del problema.
    Conviene contrastarlo, sobre todo porque en Raita et al. (2019) el mejor
    desempeno lo obtuvo una red neuronal profunda y no un arbol.

    Se comparan siete modelos de cuatro familias distintas, todos sobre el
    mismo corte de urgencia y con la misma particion:

      - Referencia:  clasificador trivial y regla sobre constantes vitales
      - Lineal:      regresion logistica
      - Agregacion:  bosque aleatorio y arboles extremadamente aleatorizados
      - Refuerzo:    potenciado por gradiente por histograma, y XGBoost o
                     LightGBM si estan instalados
      - Red neuronal: perceptron multicapa

    QUE HAY QUE MIRAR EN EL RESULTADO
    Si modelos con mecanismos tan distintos caen todos en un rango estrecho,
    eso indica que el limite no esta en el algoritmo sino en la informacion
    que contienen las variables. Es un resultado en si mismo y conviene
    escribirlo, porque convierte un desempeno modesto en una propiedad medida
    del conjunto de datos en lugar de en una carencia del trabajo.
    ===========================================================================
    """
    from sklearn.ensemble import ExtraTreesClassifier
    from sklearn.neural_network import MLPClassifier
    from sklearn.metrics import balanced_accuracy_score

    print("\n" + "=" * 79)
    print("4b. COMPARACION AMPLIADA DE FAMILIAS DE MODELOS")
    print("=" * 79)

    X = datos[VARIABLES]
    y = datos["NIVEL"].isin([1, 2]).astype(int)     # corte de urgencia
    X_e, X_p, y_e, y_p = train_test_split(
        X, y, test_size=PROPORCION_PRUEBA, stratify=y, random_state=SEMILLA)

    def tuberia(clf, escalar=False):
        pasos = [("imp", SimpleImputer(strategy="median"))]
        if escalar:
            pasos.append(("esc", StandardScaler()))
        pasos.append(("clf", clf))
        return Pipeline(pasos)

    candidatos = {}
    candidatos["Trivial"] = ("Referencia", tuberia(
        DummyClassifier(strategy="most_frequent")))
    candidatos["Regresion logistica"] = ("Lineal", tuberia(
        LogisticRegression(max_iter=2000, class_weight="balanced",
                           random_state=SEMILLA), escalar=True))
    candidatos["Bosque aleatorio"] = ("Agregacion", tuberia(
        RandomForestClassifier(n_estimators=600, min_samples_leaf=20,
                               max_features=0.5, class_weight="balanced",
                               random_state=SEMILLA, n_jobs=-1)))
    candidatos["Arboles extra aleatorios"] = ("Agregacion", tuberia(
        ExtraTreesClassifier(n_estimators=600, min_samples_leaf=20,
                             class_weight="balanced",
                             random_state=SEMILLA, n_jobs=-1)))
    candidatos["Gradiente por histograma"] = ("Refuerzo", tuberia(
        HistGradientBoostingClassifier(random_state=SEMILLA)))
    candidatos["Red neuronal (perceptron)"] = ("Red neuronal", tuberia(
        MLPClassifier(hidden_layer_sizes=(64, 32), max_iter=600,
                      early_stopping=True, random_state=SEMILLA),
        escalar=True))

    # Bibliotecas externas, solo si estan disponibles. En Colab suelen estarlo.
    try:
        from xgboost import XGBClassifier
        peso = float((y_e == 0).sum()) / max(1, int((y_e == 1).sum()))
        candidatos["XGBoost"] = ("Refuerzo", tuberia(
            XGBClassifier(n_estimators=500, max_depth=5, learning_rate=0.05,
                          subsample=0.8, colsample_bytree=0.8,
                          scale_pos_weight=peso, eval_metric="auc",
                          random_state=SEMILLA, n_jobs=-1)))
    except ImportError:
        print("  (XGBoost no esta instalado, se omite. pip install xgboost)")
    try:
        from lightgbm import LGBMClassifier
        candidatos["LightGBM"] = ("Refuerzo", tuberia(
            LGBMClassifier(n_estimators=500, num_leaves=31, learning_rate=0.05,
                           class_weight="balanced", random_state=SEMILLA,
                           n_jobs=-1, verbose=-1)))
    except ImportError:
        print("  (LightGBM no esta instalado, se omite. pip install lightgbm)")

    filas = []
    for nombre, (familia, modelo) in candidatos.items():
        modelo.fit(X_e, y_e)
        proba = modelo.predict_proba(X_p)[:, 1]
        try:
            auc = roc_auc_score(y_p, proba)
        except ValueError:
            auc = 0.5
        _, bajo, alto = auc_con_intervalo(y_p, proba, repeticiones=300)

        # La exactitud balanceada se mide en el PUNTO DE YOUDEN y no en el
        # umbral por defecto de cada modelo. Medirla en el umbral por defecto
        # penaliza a los modelos que no llevan ponderacion de clases por un
        # motivo que nada tiene que ver con su capacidad de discriminar, y eso
        # invalidaria la comparacion.
        from sklearn.metrics import roc_curve as _roc
        fpr_, tpr_, umb_ = _roc(y_p, proba)
        j_ = np.argmax(tpr_ - fpr_)
        bal = (tpr_[j_] + (1 - fpr_[j_])) / 2

        filas.append({"modelo": nombre, "familia": familia,
                      "auc": round(float(auc), 4),
                      "ic_bajo": bajo, "ic_alto": alto,
                      "bal_acc": round(float(bal), 4)})

    # La regla no es un modelo entrenado: se evalua aparte y sin AUC.
    if USAR_REGLA_CLINICA:
        pred_regla = (regla_clinica(X_p) == "ALTA").astype(int)
        filas.append({"modelo": "Regla sobre constantes", "familia": "Referencia",
                      "auc": None, "ic_bajo": None, "ic_alto": None,
                      "bal_acc": round(float(balanced_accuracy_score(y_p, pred_regla)), 4)})

    entrenados = [f for f in filas if f["auc"] is not None and f["modelo"] != "Trivial"]
    entrenados.sort(key=lambda f: -f["auc"])

    print("\n  Todas las cifras en el punto de Youden, para que la comparacion")
    print("  no dependa del umbral por defecto de cada modelo.\n")
    print("  Modelo                       Familia        AUC      IC 95 %        Bal.acc")
    for f in sorted(filas, key=lambda f: -(f["auc"] or 0)):
        auc_txt = f"{f['auc']:.4f}" if f["auc"] is not None else "   -  "
        ic_txt = (f"{f['ic_bajo']:.3f}-{f['ic_alto']:.3f}"
                  if f["ic_bajo"] is not None else "     -     ")
        print(f"  {f['modelo']:<28} {f['familia']:<13} {auc_txt}  {ic_txt}   {f['bal_acc']:.4f}")

    mejor, peor = entrenados[0], entrenados[-1]
    rango = mejor["auc"] - peor["auc"]

    print(f"\n  Mejor: {mejor['modelo']} ({mejor['auc']:.4f})")
    print(f"  Peor:  {peor['modelo']} ({peor['auc']:.4f})")
    print(f"  Rango entre el mejor y el peor: {rango:.4f} de AUC")

    if rango < 0.05:
        print("""
  LECTURA: TECHO DE INFORMACION
    Modelos con mecanismos muy distintos (lineal, agregacion, refuerzo y red
    neuronal) caen todos dentro de un rango muy estrecho. Cuando esto ocurre,
    el limite del desempeno no esta en el algoritmo sino en la informacion que
    contienen las variables de entrada. Cambiar de modelo no va a mejorar el
    resultado de forma apreciable.

    Esto es un hallazgo, no un fallo, y conviene escribirlo asi en el capitulo
    6: se comparo un abanico de familias y todas convergen, de modo que el
    desempeno observado caracteriza al conjunto de variables que un dispositivo
    de muneca puede aportar, y no a la eleccion de algoritmo. La consecuencia
    practica es que la eleccion del modelo resulta indiferente para el sistema,
    que es justo lo que el trabajo sostiene sobre su arquitectura.""")
    else:
        print(f"""
  LECTURA: LA ELECCION DE MODELO IMPORTA
    Hay {rango:.3f} de AUC entre el mejor y el peor, de modo que la familia de
    algoritmos si marca diferencia aqui. Conviene quedarse con {mejor['modelo']}
    y justificarlo con esta tabla en lugar de con la practica del dominio.""")

    resumen["comparacion_familias"] = filas
    resumen["techo_informacion"] = {"rango_auc": round(float(rango), 4),
                                    "mejor": mejor["modelo"],
                                    "auc_mejor": mejor["auc"]}
    return filas


def modelo_binario(X_ent, y_ent_bin, variables=None):
    """Bosque aleatorio con la configuracion que gano la busqueda por AUC."""
    vars_usadas = variables if variables is not None else VARIABLES
    m = Pipeline([
        ("imp", SimpleImputer(strategy="median")),
        ("clf", RandomForestClassifier(n_estimators=600,
                                       min_samples_leaf=20,
                                       max_features=0.5,
                                       class_weight="balanced",
                                       random_state=SEMILLA, n_jobs=-1)),  # noqa
    ])
    m.fit(X_ent[vars_usadas], y_ent_bin)
    return m


def evaluar_binario(modelo, X_pru, y_pru_bin, variables=None):
    """
    Devuelve las magnitudes que sirven para comparar cortes entre si.

    La clave es la EXACTITUD BALANCEADA, que es la media de sensibilidad y
    especificidad. Un clasificador que prediga siempre la clase mayoritaria
    obtiene exactamente 0,50, sea cual sea el desequilibrio del reparto. Por
    eso admite comparacion entre cortes con prevalencias distintas y no se
    puede objetar apelando a la clase mayoritaria, cosa que si le ocurre a la
    exactitud sin mas.

    El punto de operacion elegido es el de Youden, que maximiza la suma de
    sensibilidad y especificidad. No es el punto que se usara en explotacion,
    donde interesa mas sensibilidad, pero es el que permite comparar cortes
    en igualdad de condiciones.
    """
    from sklearn.metrics import roc_curve

    vars_usadas = variables if variables is not None else VARIABLES
    proba = modelo.predict_proba(X_pru[vars_usadas])[:, 1]
    auc = roc_auc_score(y_pru_bin, proba)

    fpr, tpr, umbrales = roc_curve(y_pru_bin, proba)
    j = np.argmax(tpr - fpr)          # indice de Youden
    sens, espec = tpr[j], 1 - fpr[j]
    pred = (proba >= umbrales[j]).astype(int)

    prevalencia = float(y_pru_bin.mean())
    trivial = max(prevalencia, 1 - prevalencia)

    vp = int(((pred == 1) & (y_pru_bin == 1)).sum())
    fp = int(((pred == 1) & (y_pru_bin == 0)).sum())

    return {
        "prevalencia": round(prevalencia, 4),
        "trivial": round(trivial, 4),
        "auc": round(float(auc), 4),
        "bal_acc": round(float((sens + espec) / 2), 4),
        "sensibilidad": round(float(sens), 4),
        "especificidad": round(float(espec), 4),
        "precision": round(vp / (vp + fp), 4) if (vp + fp) else 0.0,
        "exactitud": round(float((pred == y_pru_bin).mean()), 4),
    }


def comparar_cortes(datos, resumen):
    """
    ===========================================================================
    5c. QUE CORTE BINARIO DA EL MEJOR MODELO
    ===========================================================================

    Los cinco niveles se pueden partir en dos por cuatro sitios distintos, y
    cada uno da un problema diferente. Este apartado los entrena todos y los
    compara con la misma vara de medir.

    Hay dos criterios y conviene no confundirlos:

      - CUAL DA EL MEJOR MODELO. Lo dicen el AUC y la exactitud balanceada.
      - CUAL TIENE SENTIDO PARA EL SISTEMA. Eso no lo dice ningun numero.

    El corte 12|345 es el unico que separa por urgencia, porque en la escala
    de triaje los niveles 3, 4 y 5 se distinguen entre si por la prevision de
    recursos y no por la gravedad (Emergency Nurses Association, 2023). Los
    demas cortes pueden dar mejores cifras y significar menos. Si uno de ellos
    gana con claridad, la decision hay que tomarla sabiendo lo que se cambia.
    ===========================================================================
    """
    print("\n" + "=" * 79)
    print("5c. COMPARACION DE LOS CUATRO CORTES BINARIOS")
    print("=" * 79)

    X = datos[VARIABLES]
    filas = []

    for etiqueta, graves in CORTES_BINARIOS.items():
        y_bin = datos["NIVEL"].isin(graves).astype(int)
        X_e, X_p, y_e, y_p = train_test_split(
            X, y_bin, test_size=PROPORCION_PRUEBA,
            stratify=y_bin, random_state=SEMILLA
        )
        m = modelo_binario(X_e, y_e)
        r = evaluar_binario(m, X_p, y_p)
        r["corte"] = etiqueta
        r["ganancia_sobre_trivial"] = round(r["exactitud"] - r["trivial"], 4)
        filas.append(r)

    print("\n  Corte      Reparto    Trivial   AUC     Bal.acc  Sens   Espec  Precis")
    for r in filas:
        print(f"  {r['corte']:<10} "
              f"{100*r['prevalencia']:>4.1f}/{100*(1-r['prevalencia']):<4.1f} "
              f"  {r['trivial']:.3f}   {r['auc']:.3f}   {r['bal_acc']:.3f}"
              f"   {r['sensibilidad']:.3f}  {r['especificidad']:.3f}  {r['precision']:.3f}")

    print("""
  COMO LEER LA TABLA
    Reparto  = porcentaje del grupo grave frente al no grave
    Trivial  = exactitud que obtiene predecir siempre la clase mayoritaria
    Bal.acc  = exactitud balanceada. El trivial siempre saca 0,500 aqui,
               de modo que esta cifra si es comparable entre cortes y no
               admite la objecion de la clase mayoritaria
    Sens/Espec = en el punto de Youden, que es el que iguala condiciones""")

    mejor_auc = max(filas, key=lambda r: r["auc"])
    mejor_bal = max(filas, key=lambda r: r["bal_acc"])
    print(f"\n  Mejor AUC ................... {mejor_auc['corte']}  ({mejor_auc['auc']})")
    print(f"  Mejor exactitud balanceada .. {mejor_bal['corte']}  ({mejor_bal['bal_acc']})")

    urgencia = [r for r in filas if r["corte"] == "12 | 345"][0]
    print(f"\n  El corte que separa por urgencia (12|345) obtiene AUC "
          f"{urgencia['auc']} y exactitud balanceada {urgencia['bal_acc']}.")
    dif_auc = mejor_auc["auc"] - urgencia["auc"]
    if abs(dif_auc) < 0.02:
        print("  La diferencia con el mejor corte es menor de dos centesimas de AUC.")
        print("  Con esa diferencia, conviene quedarse con el corte que significa algo.")
    else:
        print(f"  El mejor corte le saca {dif_auc:+.3f} de AUC. Hay que decidir")
        print("  si esa ganancia compensa perder la frontera de urgencia.")

    resumen["comparacion_cortes"] = filas
    return filas


def ablacion_tension_arterial(datos, resumen):
    """
    ===========================================================================
    5d. CUANTO CUESTA EL CRITERIO DE SENSOR
    ===========================================================================

    El apartado 2.4.1 descarta la tension arterial porque una pulsera no la
    mide con fiabilidad. Esa decision tiene un precio en desempeno, y lo
    honesto es medirlo en lugar de suponerlo.

    Se entrena el mismo modelo con y sin las dos tensiones, sobre el corte de
    urgencia, y se compara el AUC. La diferencia es lo que cuesta la
    restriccion de dispositivo. Sea cual sea el resultado, la decision no
    cambia: una variable que el dispositivo no puede aportar no sirve aunque
    mejore el modelo. Pero poder escribir cuanto cuesta convierte una decision
    declarada en una decision medida.
    ===========================================================================
    """
    print("\n" + "=" * 79)
    print("5d. ABLACION: QUE COSTO TIENE DESCARTAR LA TENSION ARTERIAL")
    print("=" * 79)

    con_tension = VARIABLES + ["BPSYS", "BPDIAS"]
    X = datos[con_tension]
    y_bin = datos["NIVEL"].isin([1, 2]).astype(int)

    X_e, X_p, y_e, y_p = train_test_split(
        X, y_bin, test_size=PROPORCION_PRUEBA,
        stratify=y_bin, random_state=SEMILLA
    )

    m_sin = modelo_binario(X_e, y_e, variables=VARIABLES)
    r_sin = evaluar_binario(m_sin, X_p, y_p, variables=VARIABLES)

    m_con = modelo_binario(X_e, y_e, variables=con_tension)
    r_con = evaluar_binario(m_con, X_p, y_p, variables=con_tension)

    print(f"\n  Sin tension arterial (8 variables) .... AUC {r_sin['auc']}"
          f"   bal.acc {r_sin['bal_acc']}")
    print(f"  Con tension arterial (10 variables) ... AUC {r_con['auc']}"
          f"   bal.acc {r_con['bal_acc']}")
    coste = r_con["auc"] - r_sin["auc"]
    print(f"\n  Coste del criterio de sensor: {coste:+.4f} de AUC")
    if coste < 0.02:
        print("  Menos de dos centesimas. El criterio de sensor sale practicamente")
        print("  gratis, y eso refuerza el descarte del apartado 2.4.1.")
    else:
        print("  La tension arterial aporta. Aun asi el descarte se mantiene:")
        print("  una variable que la pulsera no mide no sirve en explotacion.")

    resumen["ablacion_tension"] = {"sin": r_sin, "con": r_con,
                                   "coste_auc": round(float(coste), 4)}
    return coste


def analisis_frontera_critica(X_ent, y_ent, X_pru, y_pru, resumen):
    """
    ===========================================================================
    LA PARTE QUE IMPORTA
    ===========================================================================

    La exactitud global de un problema de tres clases no dice gran cosa, y
    ademas no se puede ajustar: sale la que sale. Lo que si se puede ajustar,
    y lo que ademas es la magnitud relevante en una herramienta de peticion de
    ayuda, es la sensibilidad sobre la categoria ALTA.

    Esta funcion hace tres cosas que la comparacion anterior no hace:

    1. CALIBRA EL UMBRAL. Busca el punto de decision sobre la probabilidad de
       ALTA que alcanza la sensibilidad objetivo, y declara la precision que
       se paga por ello. Es exactamente el compromiso que describen Blomberg
       et al. (2019), que ganan sensibilidad frente al teleoperador humano y
       pierden valor predictivo positivo.

    2. COMPARA CON LA REGLA EN EL MISMO PUNTO DE OPERACION. Un modelo y una
       regla evaluados en puntos distintos no son comparables. Igualando la
       sensibilidad se puede decir cual de los dos acierta mas, que es el
       contraste que sostiene el capitulo 2 del trabajo.

    3. ENTRENA LA VARIANTE BINARIA, para poder decidir con datos si conviene
       o no. Con ella se imprime SIEMPRE la tasa de la clase mayoritaria al
       lado de la exactitud, porque una exactitud sin esa referencia no
       significa nada.
    ===========================================================================
    """
    print("\n" + "=" * 79)
    print("5b. FRONTERA CRITICA: ALTA FRENTE AL RESTO")
    print("=" * 79)

    y_ent_bin = (y_ent == CATEGORIA_CRITICA).astype(int)
    y_pru_bin = (y_pru == CATEGORIA_CRITICA).astype(int)
    prevalencia = y_pru_bin.mean()

    print(f"\n  Prevalencia de ALTA en la particion de prueba: {100*prevalencia:.1f} %")
    print(f"  Clase mayoritaria del problema binario: {100*(1-prevalencia):.1f} %")
    print("  <- Cualquier exactitud binaria hay que leerla contra esta cifra.")

    # ------------------------------------------------------------------
    # Modelo binario, con una pequena busqueda de hiperparametros guiada
    # por el area bajo la curva, que es la magnitud que interesa.
    # ------------------------------------------------------------------
    from sklearn.model_selection import GridSearchCV

    # n_jobs=1 en el bosque a proposito: la busqueda ya paraleliza por fuera
    # y anidar los dos niveles provoca el aviso de trabajadores de joblib que
    # aparecio en la ejecucion anterior.
    base = Pipeline([
        ("imp", SimpleImputer(strategy="median")),
        ("clf", RandomForestClassifier(class_weight="balanced",
                                       random_state=SEMILLA, n_jobs=1)),
    ])
    rejilla = {
        "clf__n_estimators": [300, 600],
        "clf__min_samples_leaf": [5, 20, 50],
        "clf__max_features": ["sqrt", 0.5],
    }
    print("\n  Ajustando hiperparametros por area bajo la curva ...")
    busqueda = GridSearchCV(base, rejilla, scoring="roc_auc", cv=5, n_jobs=-1)
    busqueda.fit(X_ent, y_ent_bin)
    modelo_bin = busqueda.best_estimator_
    print(f"  Mejor combinacion: {busqueda.best_params_}")
    print(f"  AUC en validacion cruzada: {busqueda.best_score_:.4f}")

    proba = modelo_bin.predict_proba(X_pru)[:, 1]
    auc = roc_auc_score(y_pru_bin, proba)
    media, bajo, alto = auc_con_intervalo(y_pru_bin, proba)
    print(f"  AUC en la particion de prueba: {auc:.4f}  "
          f"(IC 95 % {bajo:.3f} a {alto:.3f})")
    resumen["auc_intervalo"] = {"auc": round(float(auc), 4),
                                "ic_bajo": bajo, "ic_alto": alto}

    print("\n  CONCENTRACION DE RIESGO")
    print("  Si se ordenan los eventos por gravedad estimada:")
    print("    Revisando  Tasa de graves  Frente a la base  Graves capturados")
    conc = concentracion_de_riesgo(y_pru_bin, proba)
    for f in conc:
        print(f"      {100*f['fraccion_revisada']:>3.0f} %"
              f"        {100*f['tasa_en_el_grupo']:>5.1f} %"
              f"            x{f['concentracion']:<5.2f}"
              f"        {100*f['graves_capturados']:>5.1f} %")
    resumen["concentracion_riesgo"] = conc

    # ------------------------------------------------------------------
    # Curva completa de puntos de operacion
    # ------------------------------------------------------------------
    from sklearn.metrics import roc_curve

    fpr, tpr, umbrales = roc_curve(y_pru_bin, proba)
    resumen["_roc"] = {"fpr": fpr.tolist(), "tpr": tpr.tolist(),
                       "auc": float(auc)}

    def punto(sensibilidad_objetivo):
        """Devuelve el punto de operacion que alcanza esa sensibilidad."""
        i = np.argmax(tpr >= sensibilidad_objetivo)
        u = umbrales[i]
        pred = (proba >= u).astype(int)
        vp = int(((pred == 1) & (y_pru_bin == 1)).sum())
        fp = int(((pred == 1) & (y_pru_bin == 0)).sum())
        fn = int(((pred == 0) & (y_pru_bin == 1)).sum())
        vn = int(((pred == 0) & (y_pru_bin == 0)).sum())
        return {
            "umbral": round(float(u), 4),
            "sensibilidad": round(vp / (vp + fn), 4) if (vp + fn) else 0.0,
            "especificidad": round(vn / (vn + fp), 4) if (vn + fp) else 0.0,
            "precision": round(vp / (vp + fp), 4) if (vp + fp) else 0.0,
            "exactitud": round((vp + vn) / len(y_pru_bin), 4),
            "fraccion_marcada": round((vp + fp) / len(y_pru_bin), 4),
            "verdaderos_positivos": vp, "falsos_positivos": fp,
            "falsos_negativos": fn, "verdaderos_negativos": vn,
        }

    print("\n  PUNTOS DE OPERACION DEL MODELO")
    print("  La ultima columna es la clave operativa: que porcentaje de TODOS")
    print("  los eventos acabaria marcado como ALTA en ese punto. Una categoria")
    print("  que se activa la mitad de las veces deja de transmitir urgencia.\n")
    print("  Sensib.  Especif.  Precis.  Exactitud  Umbral   Eventos marcados")
    puntos = {}
    for objetivo in [0.50, 0.60, 0.70, 0.75, 0.80, 0.90]:
        p = punto(objetivo)
        puntos[f"sens_{int(objetivo*100)}"] = p
        print(f"   {p['sensibilidad']:.3f}    {p['especificidad']:.3f}"
              f"    {p['precision']:.3f}     {p['exactitud']:.3f}"
              f"     {p['umbral']:.3f}      {100*p['fraccion_marcada']:>5.1f} %")

    # ------------------------------------------------------------------
    # Comparacion pareada con la regla, en su mismo punto de operacion
    # ------------------------------------------------------------------
    if USAR_REGLA_CLINICA:
        print("\n  COMPARACION CON LA REGLA, EN SU MISMO PUNTO DE OPERACION")

        filas_franja, sin_edad, no_adultos = reparto_por_franjas(X_pru)
        print("\n    Umbrales por franja de edad, en la particion de prueba")
        print("      Franja              Pulso  Respir.   Registros")
        for f in filas_franja:
            print(f"      {f['franja']:<18} {f['pulso']:>5}  {f['respiratoria']:>6}"
                  f"   {f['n']:>9,}")
        print(f"      {'Sin edad (adulto)':<18} {UMBRAL_PULSO:>5}"
              f"  {UMBRAL_RESPIRATORIA:>6}   {sin_edad:>9,}")
        print(f"\n    Registros que NO reciben umbrales de adulto: "
              f"{no_adultos:,} de {len(X_pru):,} "
              f"({100 * no_adultos / len(X_pru):.1f} %)")
        resumen["reparto_franjas"] = {"franjas": filas_franja,
                                      "sin_edad": sin_edad,
                                      "no_adultos": no_adultos,
                                      "n_prueba": int(len(X_pru))}

        # Las dos variantes se miden contra EL MISMO modelo y LA MISMA
        # particion. Lo unico que cambia entre ellas es el umbral que la regla
        # aplica a cada registro, de modo que la diferencia entre las dos
        # cifras es limpiamente el efecto de estratificar por edad.
        variantes = [
            (False, "SIN estratificar (umbrales de adulto a todos)"),
            (True, "ESTRATIFICADA por franja de edad (ESI v5)"),
        ]

        resumen["comparacion_pareada"] = {}
        for por_franjas, titulo in variantes:
            pred_regla = (regla_clinica(X_pru, por_franjas) == "ALTA").astype(int)
            vp_r = int(((pred_regla == 1) & (y_pru_bin == 1)).sum())
            fp_r = int(((pred_regla == 1) & (y_pru_bin == 0)).sum())
            fn_r = int(((pred_regla == 0) & (y_pru_bin == 1)).sum())
            vn_r = int(((pred_regla == 0) & (y_pru_bin == 0)).sum())
            sens_regla = vp_r / (vp_r + fn_r) if (vp_r + fn_r) else 0.0
            prec_regla = vp_r / (vp_r + fp_r) if (vp_r + fp_r) else 0.0
            espec_regla = vn_r / (vn_r + fp_r) if (vn_r + fp_r) else 0.0

            emparejado = punto(sens_regla)
            mejora = emparejado["precision"] - prec_regla

            print(f"\n    REGLA {titulo}")
            print(f"      Regla   -> sensibilidad {sens_regla:.3f}"
                  f"  precision {prec_regla:.3f}"
                  f"  marca {100*(vp_r+fp_r)/len(X_pru):.1f} % de los eventos")
            print(f"      Modelo  -> sensibilidad {emparejado['sensibilidad']:.3f}"
                  f"  precision {emparejado['precision']:.3f}"
                  f"   (mismo punto de operacion)")
            if mejora > 0:
                print(f"      A igual sensibilidad, el modelo gana "
                      f"{100*mejora:.1f} puntos de precision.")
            else:
                print(f"      A igual sensibilidad la regla NO queda por debajo "
                      f"del modelo ({100*mejora:.1f} puntos).")
                print("      Hay que declararlo tal cual: es un resultado, no un fallo.")

            clave = "estratificada" if por_franjas else "sin_estratificar"
            resumen["comparacion_pareada"][clave] = {
                "regla": {"sensibilidad": round(sens_regla, 4),
                          "precision": round(prec_regla, 4),
                          "especificidad": round(espec_regla, 4),
                          "fraccion_marcada": round((vp_r + fp_r) / len(X_pru), 4)},
                "modelo_mismo_punto": emparejado,
                "ganancia_puntos": round(100 * mejora, 1),
            }

        # La cifra que va a la memoria es la de la variante activa. La otra
        # queda como analisis de sensibilidad para el capitulo 7.
        clave_activa = "estratificada" if REGLA_POR_FRANJAS else "sin_estratificar"
        g_estr = resumen["comparacion_pareada"]["estratificada"]["ganancia_puntos"]
        g_sin = resumen["comparacion_pareada"]["sin_estratificar"]["ganancia_puntos"]
        print(f"\n    EFECTO DE ESTRATIFICAR: la ventaja pasa de "
              f"{g_sin:+.1f} a {g_estr:+.1f} puntos "
              f"({g_estr - g_sin:+.1f}).")
        print(f"    Cifra que va a la memoria ({clave_activa}): "
              f"{resumen['comparacion_pareada'][clave_activa]['ganancia_puntos']:+.1f} puntos.")

        # Se conserva el punto de operacion de la variante activa, que es el
        # que la Figura 3 dibuja sobre la curva ROC.
        resumen["comparacion_pareada"]["regla"] = \
            resumen["comparacion_pareada"][clave_activa]["regla"]
        resumen["comparacion_pareada"]["modelo_mismo_punto"] = \
            resumen["comparacion_pareada"][clave_activa]["modelo_mismo_punto"]

    resumen["frontera_critica"] = {
        "prevalencia_ALTA": round(float(prevalencia), 4),
        "clase_mayoritaria_binaria": round(float(1 - prevalencia), 4),
        "auc": round(float(auc), 4),
        "mejores_hiperparametros": {k: str(v) for k, v in busqueda.best_params_.items()},
        "puntos_de_operacion": puntos,
    }

    joblib.dump({
        "modelo": modelo_bin,
        "variables": VARIABLES,
        "tipo": "binario_ALTA_vs_resto",
        "semilla": SEMILLA,
    }, DIR_SALIDA / "modelo_gravedad_binario.joblib")  # noqa

    return puntos, auc


def ejecutar(etiqueta, anios):
    """Ejecuta el analisis completo para una configuracion de anos."""
    DIR_SALIDA.mkdir(exist_ok=True)
    resumen = {"configuracion": etiqueta, "anios": anios}

    # Queda anotado en el JSON con que variante de la regla se ha medido. Sin
    # esto, dos ejecuciones producen cifras distintas y el fichero no dice por
    # que, que es justo lo que un tribunal pregunta.
    resumen["regla_referencia"] = {
        "activa": USAR_REGLA_CLINICA,
        "estratificada_por_edad": REGLA_POR_FRANJAS,
        "umbral_saturacion": UMBRAL_SATURACION,
        "umbrales_por_edad": [
            {"edad_maxima": ("inf" if e == float("inf") else e),
             "pulso": p, "respiratoria": r}
            for e, p, r in UMBRALES_POR_EDAD
        ] if REGLA_POR_FRANJAS else None,
        "umbrales_adulto": {"pulso": UMBRAL_PULSO,
                            "respiratoria": UMBRAL_RESPIRATORIA},
    }

    print("\n\n" + "#" * 79)
    print(f"#  CONFIGURACION: {etiqueta.upper()}   ->  anos {anios}")
    print("#" * 79)

    print("=" * 79)
    print("1. DESCARGA Y LECTURA")
    print("=" * 79)
    bruto = cargar_datos(anios)
    n_inicial = len(bruto)
    print(f"\n  Registros leidos: {n_inicial:,}")

    print("\n" + "=" * 79)
    print("2. LIMPIEZA Y ETIQUETA")
    print("=" * 79)
    limpio = limpiar(bruto)
    con_etiqueta = construir_etiqueta(limpio)
    n_con_etiqueta = len(con_etiqueta)

    # Se conservan tambien BPSYS y BPDIAS, que no entran en el modelo pero
    # hacen falta para la ablacion del apartado 5d. Olvidarlas aqui era el
    # motivo del error de la ejecucion anterior.
    datos = con_etiqueta[VARIABLES + ["BPSYS", "BPDIAS", "NIVEL", "GRAVEDAD"]].copy()
    # Se exige al menos una constante vital: un evento sin ningun dato
    # fisiologico no aporta nada al entrenamiento.
    hay_alguna_constante = datos[["PULSE", "POPCT", "RESPR", "TEMPF"]].notna().any(axis=1)
    datos = datos[hay_alguna_constante]
    n_final = len(datos)

    print(f"\n  EMBUDO DE REGISTROS")
    print(f"    Fichero completo ................... {n_inicial:>8,}")
    print(f"    Con nivel de triaje asignado ....... {n_con_etiqueta:>8,}"
          f"   ({100*n_con_etiqueta/n_inicial:.1f} %)")
    print(f"    Con al menos una constante vital ... {n_final:>8,}"
          f"   ({100*n_final/n_inicial:.1f} %)")

    resumen["embudo"] = {
        "registros_iniciales": int(n_inicial),
        "con_nivel_de_triaje": int(n_con_etiqueta),
        "conjunto_final": int(n_final),
    }

    print("\n  AUSENTES POR VARIABLE, sobre el conjunto final")
    for v in VARIABLES:
        pct = 100 * datos[v].isna().mean()
        print(f"    {v:<8} {pct:>5.1f} %")
    resumen["ausentes_pct"] = {v: round(100 * datos[v].isna().mean(), 2)
                               for v in VARIABLES}

    print("\n  DISTRIBUCION DE LOS CINCO NIVELES")
    dist5 = datos["NIVEL"].value_counts().sort_index()
    etiquetas5 = {1: "Inmediato", 2: "Emergente", 3: "Urgente",
                  4: "Semiurgente", 5: "No urgente"}
    for niv, n in dist5.items():
        print(f"    {niv} {etiquetas5[niv]:<12} {n:>7,}  ({100*n/n_final:.1f} %)")

    print("\n  DISTRIBUCION DE LAS TRES CATEGORIAS")
    dist3 = datos["GRAVEDAD"].value_counts()
    for cat in CATEGORIAS:
        n = int(dist3.get(cat, 0))
        print(f"    {cat:<8} {n:>7,}  ({100*n/n_final:.1f} %)")

    resumen["distribucion_5_niveles"] = {int(k): int(v) for k, v in dist5.items()}
    resumen["distribucion_3_categorias"] = {c: int(dist3.get(c, 0)) for c in CATEGORIAS}
    resumen["clase_mayoritaria_pct"] = round(100 * dist3.max() / n_final, 2)

    print("\n" + "=" * 79)
    print("3. PARTICION")
    print("=" * 79)
    X = datos[VARIABLES]
    y = datos["GRAVEDAD"]
    X_ent, X_pru, y_ent, y_pru = train_test_split(
        X, y, test_size=PROPORCION_PRUEBA, stratify=y, random_state=SEMILLA
    )
    print(f"\n  Entrenamiento: {len(X_ent):,}   Prueba: {len(X_pru):,}"
          f"   (estratificada, semilla {SEMILLA})")

    print("\n" + "=" * 79)
    print("4. ENTRENAMIENTO Y COMPARACION")
    print("=" * 79)

    # Un imputador NUEVO por cada pipeline. Compartir la misma instancia
    # funcionaria aqui por casualidad, porque todas se ajustan sobre los
    # mismos datos, pero es una fuente de errores dificiles de ver.
    def imputador():
        return SimpleImputer(strategy="median")

    modelos = {
        "Trivial (clase mayoritaria)": Pipeline([
            ("imp", imputador()),
            ("clf", DummyClassifier(strategy="most_frequent")),
        ]),
        "Regresion logistica": Pipeline([
            ("imp", imputador()),
            ("esc", StandardScaler()),
            ("clf", LogisticRegression(max_iter=1000,
                                       class_weight="balanced",
                                       random_state=SEMILLA)),
        ]),
        "Bosque aleatorio": Pipeline([
            ("imp", imputador()),
            ("clf", RandomForestClassifier(n_estimators=300,
                                           min_samples_leaf=5,
                                           class_weight="balanced",
                                           random_state=SEMILLA,
                                           n_jobs=-1)),
        ]),
        "Arbol potenciado por gradiente": Pipeline([
            ("imp", imputador()),
            ("clf", HistGradientBoostingClassifier(random_state=SEMILLA)),
        ]),
    }

    filas = []
    entrenados = {}

    for nombre, modelo in modelos.items():
        modelo.fit(X_ent, y_ent)
        pred = modelo.predict(X_pru)
        entrenados[nombre] = modelo

        exactitud = accuracy_score(y_pru, pred)
        sens_alta = recall_score(y_pru, pred, labels=[CATEGORIA_CRITICA],
                                 average="macro", zero_division=0)
        prec_alta = precision_score(y_pru, pred, labels=[CATEGORIA_CRITICA],
                                    average="macro", zero_division=0)

        # Area bajo la curva de ALTA frente al resto
        try:
            proba = modelo.predict_proba(X_pru)
            idx = list(modelo.classes_).index(CATEGORIA_CRITICA)
            auc = roc_auc_score((y_pru == CATEGORIA_CRITICA).astype(int),
                                proba[:, idx])
        except Exception:
            auc = float("nan")

        filas.append({
            "Modelo": nombre,
            "Exactitud": round(exactitud, 4),
            "Sensib. ALTA": round(sens_alta, 4),
            "Precis. ALTA": round(prec_alta, 4),
            "AUC ALTA": round(auc, 4) if auc == auc else None,
        })

    # La regla de referencia se evalua aparte: no es un modelo entrenado.
    if USAR_REGLA_CLINICA:
        pred_regla = regla_clinica(X_pru)
        filas.append({
            "Modelo": "Regla sobre constantes (referencia)",
            "Exactitud": round(accuracy_score(y_pru, pred_regla), 4),
            "Sensib. ALTA": round(recall_score(y_pru, pred_regla,
                                               labels=[CATEGORIA_CRITICA],
                                               average="macro",
                                               zero_division=0), 4),
            "Precis. ALTA": round(precision_score(y_pru, pred_regla,
                                                  labels=[CATEGORIA_CRITICA],
                                                  average="macro",
                                                  zero_division=0), 4),
            "AUC ALTA": None,
        })

    tabla = pd.DataFrame(filas)
    print("\n" + tabla.to_string(index=False))
    resumen["comparacion_modelos"] = filas

    comparar_familias(datos, resumen)

    print("\n" + "=" * 79)
    print("5. MODELO PRINCIPAL: BOSQUE ALEATORIO")
    print("=" * 79)

    principal = entrenados["Bosque aleatorio"]
    pred = principal.predict(X_pru)

    print("\n  MATRIZ DE CONFUSION")
    print("  (filas: categoria real / columnas: categoria predicha)\n")
    mc = confusion_matrix(y_pru, pred, labels=CATEGORIAS)
    print("           " + "".join(f"{c:>10}" for c in CATEGORIAS))
    for i, c in enumerate(CATEGORIAS):
        print(f"  {c:<8} " + "".join(f"{v:>10,}" for v in mc[i]))

    print("\n  INFORME DE CLASIFICACION\n")
    print(classification_report(y_pru, pred, labels=CATEGORIAS,
                                zero_division=0, digits=3))

    # ------------------------------------------------------------------
    # Error grave: confundir los dos extremos de la escala.
    # No todos los errores pesan igual. Que un evento MEDIA se clasifique como
    # BAJA es un desliz; que uno ALTA se clasifique como BAJA es el fallo que
    # importa, porque es el que deja sin prioridad una situacion grave. Esta
    # distincion es habitual en la literatura de concordancia de triaje y
    # conviene reportarla, porque una exactitud global no la recoge.
    # ------------------------------------------------------------------
    i_alta, i_baja = CATEGORIAS.index("ALTA"), CATEGORIAS.index("BAJA")
    total = int(mc.sum())
    graves_como_baja = int(mc[i_alta, i_baja])
    bajas_como_alta = int(mc[i_baja, i_alta])
    error_grave = (graves_como_baja + bajas_como_alta) / total
    reales_alta = int(mc[i_alta].sum())

    print("\n  GRAVEDAD DE LOS ERRORES")
    print(f"    Predicciones con error de dos niveles ... "
          f"{graves_como_baja + bajas_como_alta:,} de {total:,}"
          f"   ({100*error_grave:.1f} %)")
    print(f"    Acierto dentro de un nivel ............. {100*(1-error_grave):.1f} %")
    print(f"    Eventos ALTA clasificados como BAJA .... {graves_como_baja:,}"
          f" de {reales_alta:,}   ({100*graves_como_baja/reales_alta:.1f} %)")
    print("      <- Esta ultima es la cifra que importa para la seguridad del")
    print("         sistema, y la que justifica desplazar el punto de decision")
    print("         hacia la sensibilidad.")

    resumen["gravedad_errores"] = {
        "error_dos_niveles_pct": round(100 * error_grave, 2),
        "acierto_un_nivel_pct": round(100 * (1 - error_grave), 2),
        "alta_como_baja": graves_como_baja,
        "alta_como_baja_pct": round(100 * graves_como_baja / reales_alta, 2),
    }

    resumen["matriz_confusion"] = {
        "orden": CATEGORIAS,
        "valores": mc.tolist(),
    }
    resumen["informe"] = classification_report(
        y_pru, pred, labels=CATEGORIAS, zero_division=0,
        digits=3, output_dict=True
    )

    print("  IMPORTANCIA DE LAS VARIABLES")
    importancias = pd.Series(
        principal.named_steps["clf"].feature_importances_, index=VARIABLES
    ).sort_values(ascending=False)
    for var, imp in importancias.items():
        barra = "#" * int(round(imp * 60))
        print(f"    {var:<8} {imp:>6.3f}  {barra}")
    resumen["importancia_variables"] = {k: round(float(v), 4)
                                        for k, v in importancias.items()}

    puntos, auc_bin = analisis_frontera_critica(X_ent, y_ent, X_pru, y_pru, resumen)
    filas_cortes = comparar_cortes(datos, resumen)
    coste_tension = ablacion_tension_arterial(datos, resumen)

    print("\n" + "=" * 79)
    print("6. GUARDADO")
    print("=" * 79)

    sufijo = etiqueta.lower().replace(" ", "_")
    ruta_modelo = DIR_SALIDA / f"modelo_gravedad_{sufijo}.joblib"
    joblib.dump({
        "modelo": principal,
        "variables": VARIABLES,
        "categorias": CATEGORIAS,
        "semilla": SEMILLA,
        "anios": anios,
    }, ruta_modelo)
    print(f"\n  Modelo guardado en: {ruta_modelo}")
    print("  Este es el unico fichero que el microservicio necesita.")

    ruta_metricas = DIR_SALIDA / f"metricas_{sufijo}.json"
    with open(ruta_metricas, "w", encoding="utf-8") as f:
        json.dump({k: v for k, v in resumen.items() if not k.startswith("_")},
                  f, indent=2, ensure_ascii=False)
    print(f"  Metricas guardadas en: {ruta_metricas}")

    # -------------------------------------------------------------------------
    print("\n" + "=" * 79)
    print("RESUMEN PARA LA MEMORIA")
    print("Copia estas cifras a los marcadores PENDIENTE-MEDICION")
    print("=" * 79)

    fila_rf = [f for f in filas if f["Modelo"] == "Bosque aleatorio"][0]
    print(f"""
  APARTADO 5.10 y CAPITULO 6 - conjunto de datos
    Registros del fichero .................. {n_inicial:,}
    Con nivel de triaje asignado ........... {n_con_etiqueta:,}
    CONJUNTO FINAL ......................... {n_final:,}   <- marcador de A.2.1

  CAPITULO 6 - reparto de clases
    Clase mayoritaria ...................... {resumen['clase_mayoritaria_pct']} %
    (el detalle por nivel esta en metricas.json)

  CAPITULO 6 - resultados del modelo principal
    Exactitud global ....................... {fila_rf['Exactitud']}
    Sensibilidad de ALTA ................... {fila_rf['Sensib. ALTA']}
    Precision de ALTA ...................... {fila_rf['Precis. ALTA']}
    AUC de ALTA frente al resto ............ {fila_rf['AUC ALTA']}

  ANEXO D - reproducibilidad
    Semilla ................................ {SEMILLA}
    Particion .............................. {int((1-PROPORCION_PRUEBA)*100)}-{int(PROPORCION_PRUEBA*100)} estratificada
    Anos empleados ......................... {anios}
""")

    p75 = puntos.get("sens_75", {})
    print(f"""
  CAPITULO 6 - frontera critica ALTA frente al resto
    Prevalencia de ALTA .................... {100*resumen['frontera_critica']['prevalencia_ALTA']:.1f} %
    Clase mayoritaria del binario .......... {100*resumen['frontera_critica']['clase_mayoritaria_binaria']:.1f} %
    AUC .................................... {auc_bin:.4f}
    En el punto de sensibilidad 0,75:
      sensibilidad ......................... {p75.get('sensibilidad')}
      precision ............................ {p75.get('precision')}
      especificidad ........................ {p75.get('especificidad')}
      exactitud ............................ {p75.get('exactitud')}
""")

    mejor_bal = max(filas_cortes, key=lambda r: r["bal_acc"])
    urgencia = [r for r in filas_cortes if r["corte"] == "12 | 345"][0]

    print(f"""
  CAPITULO 6 - comparacion de cortes binarios
    Mejor exactitud balanceada ............. {mejor_bal['corte']}  ({mejor_bal['bal_acc']})
    Corte de urgencia (12|345) ............. AUC {urgencia['auc']}  bal.acc {urgencia['bal_acc']}
    Coste de descartar la tension arterial . {coste_tension:+.4f} de AUC

  LAS CIFRAS QUE SI SE PUEDEN ESCRIBIR SIN INCOMODIDAD
    Exactitud balanceada ................... {urgencia['bal_acc']}
      El clasificador trivial saca 0,500 en esta metrica sea cual sea el
      desequilibrio, de modo que no admite la objecion de la clase mayoritaria.
    AUC .................................... {urgencia['auc']}
      Es la magnitud que reportan Raita et al. (2019) sobre este mismo conjunto.
    Ganancia sobre la regla de referencia ... ver apartado 5b
      A igual sensibilidad, cuantos puntos de precision gana el modelo frente
      a un conjunto de reglas clinicas fijas. Es el contraste del capitulo 2
      medido con datos propios, y es el mejor resultado del trabajo.

  LA CIFRA QUE NO SE DEBE ESCRIBIR SOLA
    La exactitud, en cualquiera de los cortes. Siempre acompanada de la tasa
    de la clase mayoritaria, o mejor sustituida por la exactitud balanceada.

  ---------------------------------------------------------------------------
  GLOSARIO: COMO SE LEE EL INFORME DE CLASIFICACION
  ---------------------------------------------------------------------------
    Se lee POR FILAS, una fila por categoria. Para la fila ALTA:

    support      Cuantos eventos ALTA hay de verdad en la particion de prueba.
    recall       De esos, que proporcion detecto el modelo. Es la sensibilidad.
                 Recall bajo = se escapan situaciones graves.
    precision    De los que el modelo LLAMO ALTA, que proporcion lo era.
                 Precision baja = muchas alarmas que no lo eran.
    f1-score     Media armonica de las dos anteriores. Resume, pero esconde
                 cual de las dos flojea, asi que conviene mirar las dos.

    Al pie del informe:

    accuracy     Proporcion global de aciertos. Con clases desequilibradas
                 dice poco, porque la clase mayoritaria la domina.
    macro avg    Media SIN ponderar de las tres categorias. Trata a la clase
                 pequena igual que a la grande. Es la que hay que mirar aqui.
    weighted avg Media PONDERADA por el numero de casos de cada categoria.
                 La domina la clase mayoritaria, de modo que se parece mucho
                 a la exactitud y aporta poco.

    En la matriz de confusion: las FILAS son la categoria real y las COLUMNAS
    la predicha. La diagonal son los aciertos. Lo que hay fuera de la diagonal
    dice CON QUE se confunde cada categoria, que es mas util que saber cuanto
    falla.

  ---------------------------------------------------------------------------
  VEREDICTO: LAS SEIS DECISIONES Y EL NUMERO QUE LAS SOSTIENE
  ---------------------------------------------------------------------------
    Cada decision del modulo queda respaldada por una comparacion medida y no
    por un criterio declarado. Esto es lo que hay que llevar al capitulo 6.

    1. ANOS EMPLEADOS
       Se comparan un ano y cuatro. La comparacion final del script da los
       registros y el desempeno de cada uno.

    2. FAMILIA DE ALGORITMOS  -> apartado 4b, figura 2
       Siete modelos de cuatro familias. Si el rango entre el mejor y el peor
       es estrecho, la eleccion resulta indiferente y el limite esta en las
       variables. Ese es el resultado, y es mas informativo que decir que los
       arboles son la practica del dominio.

    3. NUMERO DE CATEGORIAS  -> apartado 5c
       Se comparan los cuatro cortes binarios posibles. Se conserva el corte
       que separa por urgencia salvo que otro le saque una ventaja apreciable.

    4. DESCARTE DE LA TENSION ARTERIAL  -> apartado 5d
       El coste en area bajo la curva esta medido. La decision pasa de estar
       declarada a estar cuantificada.

    5. PUNTO DE OPERACION  -> apartado 5b
       La tabla de puntos incluye que porcentaje de eventos quedaria marcado
       como ALTA en cada uno. Subir la sensibilidad inunda la categoria, y esa
       tension conviene declararla en lugar de elegir el punto en silencio.

    6. UTILIDAD FRENTE A UNAS REGLAS FIJAS  -> apartado 5b, figura 3
       Puntos de precision que el modulo gana a la regla clinica a igual
       sensibilidad. Es el contraste que plantea el apartado 2.5 del TFM,
       reproducido con datos propios, y es el mejor resultado del trabajo.

    Y una cifra que conviene dar uno mismo antes de que la pregunten: el
    porcentaje de eventos realmente graves que acaban clasificados como BAJA,
    que aparece en el apartado 5 bajo GRAVEDAD DE LOS ERRORES.
  ---------------------------------------------------------------------------

  FIGURAS GENERADAS, listas para el capitulo 6
    fig1  reparto de clases
    fig2  comparacion de las siete familias de modelos
    fig3  curva ROC con el punto de la regla marcado
    fig4  matriz de confusion
    fig5  importancia de las variables, coloreada por origen
    fig6  concentracion de los casos graves
  ---------------------------------------------------------------------------
""")

    import sklearn
    print(f"    pandas {pd.__version__} / scikit-learn {sklearn.__version__} "
          f"/ numpy {np.__version__}")
    print("=" * 79)

    generar_figuras(datos, resumen, mc, importancias, sufijo)

    resumen["_para_comparar"] = {
        "n": n_final,
        "auc_urgencia": urgencia["auc"],
        "bal_acc_urgencia": urgencia["bal_acc"],
        "mejor_corte": mejor_bal["corte"],
        "auc_mejor_corte": mejor_bal["auc"],
        "coste_tension": round(float(coste_tension), 4),
        "ganancia_sobre_regla": resumen.get("comparacion_pareada", {})
                                      .get("modelo_mismo_punto", {})
                                      .get("precision", 0)
                               - resumen.get("comparacion_pareada", {})
                                      .get("regla", {})
                                      .get("precision", 0),
    }
    return resumen


# =============================================================================
# 7. PROGRAMA PRINCIPAL: LAS DOS CONFIGURACIONES EN UNA SOLA PASADA
# =============================================================================

def main():
    resultados = {}
    for etiqueta, anios in CONFIGURACIONES.items():
        resultados[etiqueta] = ejecutar(etiqueta, anios)

    print("\n\n" + "#" * 79)
    print("#  COMPARACION FINAL ENTRE CONFIGURACIONES")
    print("#" * 79)
    print("""
  Un ano da menos registros y cifras mas inestables, sobre todo en el nivel 1,
  que es el mas escaso. Agregar anos multiplica los casos y estabiliza el
  resultado, a cambio de tener que declarar en la memoria que se combinan
  ejercicios, cosa sobre la que el propio NCHS pide cautela.
""")
    print("  Configuracion     Registros    AUC urgencia   Bal.acc   Gana a la regla")
    for etiqueta, r in resultados.items():
        c = r["_para_comparar"]
        print(f"  {etiqueta:<16}  {c['n']:>8,}      {c['auc_urgencia']:.4f}"
              f"       {c['bal_acc_urgencia']:.4f}    {100*c['ganancia_sobre_regla']:>+5.1f} puntos")

    print("""
  QUE MIRAR
    Registros        -> cuanto mas, mas creible es la cifra en la memoria
    AUC urgencia     -> discriminacion sobre el corte que separa por urgencia
    Bal.acc          -> exactitud balanceada; el trivial saca 0,500 siempre
    Gana a la regla  -> puntos de precision sobre la regla clinica, a igual
                        sensibilidad. Es el contraste del capitulo 2 y el
                        mejor resultado del trabajo

  La configuracion que gane en registros y en ganancia sobre la regla es la
  que conviene llevar a la memoria, aunque su AUC sea similar.
""")
    print("#" * 79)
    return resultados


# =============================================================================
# 8. CIERRE: BLOQUE PARA COPIAR Y DESCARGA DE FICHEROS
#    Todo lo que sigue es para la ejecucion en Colab. No afecta al analisis.
# =============================================================================

def bloque_para_copiar(resultados):
    """
    Imprime un bloque compacto con todo lo que hace falta para actualizar la
    memoria. Se copia entero de una sola seleccion.
    """
    print("\n\n" + "=" * 79)
    print("BLOQUE PARA COPIAR")
    print("Selecciona desde la linea de abajo hasta el final y copialo entero.")
    print("=" * 79)
    print("\n" + "-" * 79)

    for etiqueta, r in resultados.items():
        cp = r.get("comparacion_pareada", {})
        fc = r.get("frontera_critica", {})
        ge = r.get("gravedad_errores", {})
        rf = r.get("reparto_franjas", {})
        conc = r.get("concentracion_riesgo", []) or []

        print(f"\nCONFIGURACION: {etiqueta}   anos {r.get('anios')}")
        print(f"  Registros finales      {r.get('_para_comparar', {}).get('n', '?'):,}")
        print(f"  Prevalencia ALTA       {100*fc.get('prevalencia_ALTA', 0):.1f} %")
        print(f"  Clase mayoritaria      {100*fc.get('clase_mayoritaria_binaria', 0):.1f} %")
        ai = r.get("auc_intervalo", {})
        print(f"  AUC ALTA vs resto      {ai.get('auc', fc.get('auc', 0)):.4f}"
              f"   IC 95 % [{ai.get('ic_bajo', '?')} - {ai.get('ic_alto', '?')}]")
        print(f"  Exactitud balanceada   "
              f"{r.get('_para_comparar', {}).get('bal_acc_urgencia', 0):.4f}")
        if ge:
            print(f"  ALTA clasificada BAJA  {ge.get('alta_como_baja_pct', 0):.1f} %"
                  f"  ({ge.get('alta_como_baja', 0):,} casos)")
        if conc:
            print(f"  Concentracion 1er decil x{conc[0].get('concentracion', 0):.2f}")

        if rf:
            print(f"\n  REPARTO POR FRANJAS (particion de prueba, n={rf.get('n_prueba', 0):,})")
            for f in rf.get("franjas", []):
                print(f"    {f['franja']:<18} pulso>{f['pulso']:<4}"
                      f" resp>{f['respiratoria']:<4} n={f['n']:,}")
            print(f"    Sin edad registrada: {rf.get('sin_edad', 0):,}")
            print(f"    No adultos: {rf.get('no_adultos', 0):,}"
                  f" ({100*rf.get('no_adultos', 0)/max(rf.get('n_prueba', 1), 1):.1f} %)")

        for clave, titulo in [("sin_estratificar", "REGLA SIN ESTRATIFICAR (original)"),
                              ("estratificada", "REGLA ESTRATIFICADA POR EDAD (ESI v5)")]:
            d = cp.get(clave)
            if not d:
                continue
            print(f"\n  {titulo}")
            print(f"    Regla  sensibilidad {d['regla']['sensibilidad']:.4f}"
                  f"  precision {d['regla']['precision']:.4f}"
                  f"  especificidad {d['regla']['especificidad']:.4f}")
            print(f"    Modelo sensibilidad {d['modelo_mismo_punto']['sensibilidad']:.4f}"
                  f"  precision {d['modelo_mismo_punto']['precision']:.4f}")
            print(f"    GANANCIA {d['ganancia_puntos']:+.1f} puntos")

        imp = r.get("importancia_variables", {})
        if imp:
            top = list(imp.items())[:4]
            print("\n  IMPORTANCIA (top 4): "
                  + ", ".join(f"{k} {v:.3f}" for k, v in top))

    print("\n" + "-" * 79)
    print("FIN DEL BLOQUE PARA COPIAR")
    print("-" * 79)


def descargar_resultados():
    """Empaqueta la carpeta de salida y la descarga. Solo tiene efecto en Colab."""
    import shutil
    if not DIR_SALIDA.exists():
        print("\n  No hay carpeta de salida que descargar.")
        return

    archivo = shutil.make_archive("salida_modelo", "zip", str(DIR_SALIDA))
    print(f"\n  Empaquetado: {archivo}")
    print("  Contenido:")
    for p in sorted(DIR_SALIDA.iterdir()):
        print(f"    {p.name}  ({p.stat().st_size / 1024:.0f} KB)")

    try:
        from google.colab import files
        print("\n  Lanzando la descarga del zip...")
        files.download(archivo)
    except Exception:
        print("\n  (No se detecta Colab: el zip queda en el directorio actual.)")


if __name__ == "__main__":
    resultados = main()
    bloque_para_copiar(resultados)
    descargar_resultados()