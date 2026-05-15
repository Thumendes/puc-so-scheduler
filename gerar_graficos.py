#!/usr/bin/env python3
"""Gera gráficos comparativos dos algoritmos de escalonamento."""

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import re
from pathlib import Path

ROOT = Path(__file__).parent
RESULTADOS = ROOT / "resultados"
GRAFICOS = ROOT / "graficos"
GRAFICOS.mkdir(exist_ok=True)

ALGO_LABELS = {
    "FCFS": "FCFS",
    "SRTF": "SRTF",
    "Round-Robin com Quantum por Predicao": "RR",
    "MLQ": "MLQ",
}
ALGOS = ["FCFS", "SRTF", "RR", "MLQ"]
COLORS = ["#4C72B0", "#DD8452", "#55A868", "#C44E52"]


def parse_file(path):
    data = {}
    algo = None
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if m := re.match(r"=== (.+) ===", line):
            algo = ALGO_LABELS.get(m.group(1), m.group(1))
            data[algo] = {}
        elif algo:
            if m := re.search(r"Espera Medio\s*:\s*([\d.]+)", line):
                data[algo]["espera"] = float(m.group(1))
            elif m := re.search(r"Turnaround Medio\s*:\s*([\d.]+)", line):
                data[algo]["turnaround"] = float(m.group(1))
            elif m := re.search(r"Throughput\s*:\s*([\d.]+)", line):
                data[algo]["throughput"] = float(m.group(1))
    return data


# ── Carrega resultados ──────────────────────────────────────────────────────

principal = parse_file(RESULTADOS / "principal.txt")

cenarios = {
    "Principal\n(12 proc.)": principal,
    "t1\n(todos t=0)":       parse_file(RESULTADOS / "t1_todos_ao_mesmo_tempo.txt"),
    "t2\n(sem I/O)":         parse_file(RESULTADOS / "t2_sem_io.txt"),
    "t3\n(I/O intensivo)":   parse_file(RESULTADOS / "t3_io_intensivo.txt"),
    "t5\n(burst misto)":     parse_file(RESULTADOS / "t5_burst_misto.txt"),
    "t6\n(chegada tardia)":  parse_file(RESULTADOS / "t6_chegada_tardia.txt"),
}
# t4 excluído: resultado idêntico para todos os algoritmos


# ── Gráfico 1: comparação no arquivo principal ──────────────────────────────

fig, axes = plt.subplots(1, 3, figsize=(14, 5))
fig.suptitle("Arquivo principal — 12 processos", fontsize=13, fontweight="bold")

metrics = [
    ("espera",      "Espera Média (ms)",      "Menor é melhor"),
    ("turnaround",  "Turnaround Médio (ms)",  "Menor é melhor"),
    ("throughput",  "Throughput (proc/ms)",   "Maior é melhor"),
]

for ax, (key, ylabel, caption) in zip(axes, metrics):
    values = [principal[a][key] for a in ALGOS]
    bars = ax.bar(ALGOS, values, color=COLORS, edgecolor="white", linewidth=0.8)
    ax.set_title(ylabel, fontsize=10, pad=8)
    ax.set_ylabel(ylabel, fontsize=8)
    ax.set_xlabel(caption, fontsize=8, color="gray")
    ax.tick_params(axis="x", labelsize=9)
    ax.yaxis.set_tick_params(labelsize=8)
    ax.spines[["top", "right"]].set_visible(False)
    for bar, val in zip(bars, values):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            bar.get_height() + max(values) * 0.01,
            f"{val:.4f}" if key == "throughput" else f"{val:.2f}",
            ha="center", va="bottom", fontsize=8,
        )
    if key != "throughput":
        best_idx = int(np.argmin(values))
    else:
        best_idx = int(np.argmax(values))
    bars[best_idx].set_edgecolor("#222")
    bars[best_idx].set_linewidth(2)

plt.tight_layout()
out = GRAFICOS / "1_principal_comparativo.png"
plt.savefig(out, dpi=150, bbox_inches="tight")
plt.close()
print(f"Salvo: {out}")


# ── Gráfico 2: espera média por cenário ────────────────────────────────────

fig, ax = plt.subplots(figsize=(13, 5))
labels = list(cenarios.keys())
x = np.arange(len(labels))
width = 0.18

for i, (algo, color) in enumerate(zip(ALGOS, COLORS)):
    values = [cenarios[label].get(algo, {}).get("espera", 0) for label in labels]
    offset = (i - 1.5) * width
    bars = ax.bar(x + offset, values, width, label=algo, color=color,
                  edgecolor="white", linewidth=0.6)
    for bar, val in zip(bars, values):
        if val > 0:
            ax.text(bar.get_x() + bar.get_width() / 2,
                    bar.get_height() + 0.4,
                    f"{val:.1f}", ha="center", va="bottom", fontsize=7)

ax.set_title("Tempo de Espera Médio por Cenário", fontsize=12, fontweight="bold")
ax.set_ylabel("Espera Média (ms)", fontsize=10)
ax.set_xticks(x)
ax.set_xticklabels(labels, fontsize=9)
ax.legend(fontsize=9)
ax.spines[["top", "right"]].set_visible(False)
plt.tight_layout()
out = GRAFICOS / "2_espera_por_cenario.png"
plt.savefig(out, dpi=150, bbox_inches="tight")
plt.close()
print(f"Salvo: {out}")


# ── Gráfico 3: turnaround médio por cenário ────────────────────────────────

fig, ax = plt.subplots(figsize=(13, 5))

for i, (algo, color) in enumerate(zip(ALGOS, COLORS)):
    values = [cenarios[label].get(algo, {}).get("turnaround", 0) for label in labels]
    offset = (i - 1.5) * width
    bars = ax.bar(x + offset, values, width, label=algo, color=color,
                  edgecolor="white", linewidth=0.6)
    for bar, val in zip(bars, values):
        if val > 0:
            ax.text(bar.get_x() + bar.get_width() / 2,
                    bar.get_height() + 0.4,
                    f"{val:.1f}", ha="center", va="bottom", fontsize=7)

ax.set_title("Turnaround Médio por Cenário", fontsize=12, fontweight="bold")
ax.set_ylabel("Turnaround Médio (ms)", fontsize=10)
ax.set_xticks(x)
ax.set_xticklabels(labels, fontsize=9)
ax.legend(fontsize=9)
ax.spines[["top", "right"]].set_visible(False)
plt.tight_layout()
out = GRAFICOS / "3_turnaround_por_cenario.png"
plt.savefig(out, dpi=150, bbox_inches="tight")
plt.close()
print(f"Salvo: {out}")


# ── Gráfico 4: radar — desempenho normalizado no arquivo principal ──────────
# Métricas: espera (inverso), turnaround (inverso), throughput (direto)

fig, ax = plt.subplots(figsize=(6, 6), subplot_kw={"projection": "polar"})

metric_keys  = ["espera", "turnaround", "throughput"]
metric_names = ["1 / Espera", "1 / Turnaround", "Throughput"]
N = len(metric_names)
angles = np.linspace(0, 2 * np.pi, N, endpoint=False).tolist()
angles += angles[:1]

raw = {a: [principal[a][k] for k in metric_keys] for a in ALGOS}

# Normaliza: para espera e turnaround usa inverso (menor = melhor → maior pontuação)
def normalize(values_per_algo, invert_flags):
    cols = list(zip(*values_per_algo.values()))
    norm = {}
    for algo, vals in values_per_algo.items():
        norm_vals = []
        for v, col, inv in zip(vals, cols, invert_flags):
            mn, mx = min(col), max(col)
            if mx == mn:
                norm_vals.append(0.5)
            elif inv:
                norm_vals.append(1 - (v - mn) / (mx - mn))
            else:
                norm_vals.append((v - mn) / (mx - mn))
        norm[algo] = norm_vals
    return norm

invert = [True, True, False]  # espera↓ turnaround↓ throughput↑
normed = normalize(raw, invert)

for algo, color in zip(ALGOS, COLORS):
    vals = normed[algo] + normed[algo][:1]
    ax.plot(angles, vals, color=color, linewidth=2, label=algo)
    ax.fill(angles, vals, color=color, alpha=0.12)

ax.set_xticks(angles[:-1])
ax.set_xticklabels(metric_names, fontsize=10)
ax.set_yticklabels([])
ax.set_title("Desempenho Normalizado\n(arquivo principal)", fontsize=11,
             fontweight="bold", pad=20)
ax.legend(loc="upper right", bbox_to_anchor=(1.35, 1.1), fontsize=9)
ax.spines["polar"].set_visible(False)

plt.tight_layout()
out = GRAFICOS / "4_radar_normalizado.png"
plt.savefig(out, dpi=150, bbox_inches="tight")
plt.close()
print(f"Salvo: {out}")

print("\nTodos os gráficos salvos em graficos/")
