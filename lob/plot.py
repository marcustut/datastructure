import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

fig, ax = plt.subplots()

file_path = "app/src/main/resources/benchmark.csv"
df = pd.read_csv(file_path)
df["opThroughput"] = df["count"] / (df["opDuration"] / 1e9)


def plot_all_duration():
    xticks = np.arange(1, 11)
    plt.bar(xticks, df["opDuration"] / 1e6)

    for idx, row in df.iterrows():
        plt.annotate(
            "{:.2f}ms".format(row["opDuration"] / 1e6),
            (xticks[idx], row["opDuration"] / 1e6),
            xytext=(xticks[idx] - 0.45, (row["opDuration"] / 1e6) + 2),
            size=6,
        )

    plt.xlabel("Runs")
    plt.ylabel("Time taken (ms)")
    plt.xticks(xticks)
    plt.title("Time taken across 10 runs")
    plt.savefig("images/benchmark_time_taken.png", bbox_inches="tight")
    plt.clf()


def plot_all_latency():
    xticks = np.arange(1, 11)
    plt.bar(xticks, df["opDuration"] / df["count"])

    for idx, row in df.iterrows():
        plt.annotate(
            "{:.2f}ns".format(row["opDuration"] / row["count"]),
            (xticks[idx], row["opDuration"] / row["count"]),
            xytext=(xticks[idx] - 0.45, (row["opDuration"] / row["count"]) + 2),
            size=6,
        )

    plt.xlabel("Runs")
    plt.ylabel("Average Latency (ns)")
    plt.xticks(xticks)
    plt.title("Average latency per operation across 10 runs")
    plt.savefig("images/benchmark_latency.png", bbox_inches="tight")
    plt.clf()


def plot_all_throughput():
    average = 0

    xticks = np.arange(1, 11)
    plt.bar(xticks, df["opThroughput"])

    for idx, row in df.iterrows():
        average += row["opThroughput"]
        plt.annotate(
            "{}".format(round(row["opThroughput"])),
            (xticks[idx], row["opThroughput"]),
            xytext=(xticks[idx] - 0.4, row["opThroughput"] + 0.5e5),
            size=6,
        )

    average = average / df.shape[0]
    print(f"Average throughput: {round(average)}")

    plt.xlabel("Runs")
    plt.ylabel("Throughput (op/s)")
    plt.xticks(xticks)
    plt.title("Throughput across 10 runs")
    plt.savefig("images/benchmark_throughput.png", bbox_inches="tight")
    plt.clf()


plot_all_duration()
plot_all_latency()
plot_all_throughput()
