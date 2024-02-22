import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as tkr

fig, ax = plt.subplots()

file_path = 'data/benchmark.csv'
df = pd.read_csv(file_path)
df['throughput'] = (df['ops'] / df['duration']) * 1e9

poll_df = df[df['name'].str.contains('poll')]
poll_df['name'].update(poll_df['name'].map(lambda n: n[1:].split('-')[0]))

offer_df = df[df['name'].str.contains('offer')]
offer_df['name'].update(offer_df['name'].map(lambda n: n[1:].split('-')[0]))

def format(num, pos):
    if num > 1000000:
        if not num % 1000000:
            return f'{num // 1000000}M'
        return f'{round(num / 1000000, 1)}M'
    return f'{num // 1000}K'

# Iterate through unique 'name' values for 'poll' action
for name in poll_df['name'].unique():
    subset_df = poll_df[(poll_df['name'] == name)]
    plt.plot(subset_df['ops'], subset_df['duration'] / 1e6, label=name)

plt.xlabel('Number of operations')
plt.ylabel('Duration (ms)')
ax.xaxis.set_major_formatter(tkr.FuncFormatter(format))
plt.title('Queue - poll')
plt.legend()
plt.savefig(f'images/benchmark_poll.png', bbox_inches='tight')
plt.clf()

# Iterate through unique 'name' values for 'offer' action
for name in offer_df['name'].unique():
    subset_df = offer_df[(offer_df['name'] == name)]
    plt.plot(subset_df['ops'], subset_df['duration'] / 1e6, label=name)

plt.xlabel('Number of operations')
plt.ylabel('Duration (ms)')
ax.xaxis.set_major_formatter(tkr.FuncFormatter(format))
plt.title('Queue - offer')
plt.legend()
plt.savefig(f'images/benchmark_offer.png', bbox_inches='tight')
plt.clf()

# Define a color map for 'name' values
name_colors = plt.cm.rainbow(np.linspace(0, 1, len(df['name'].unique())))

# Iterate through unique 'ops' values
for df, action in [(poll_df, 'poll'), (offer_df, 'offer')]:
    for ops in df['ops'].unique():
        subset_df = df[df['ops'] == ops]

        # Get unique 'name' values for the current 'ops' value
        unique_names = subset_df['name'].unique()

        # Define a color map for the current 'ops' value
        name_colors = plt.cm.rainbow(np.linspace(0, 1, len(unique_names)))

        fig, ax = plt.subplots()

        # Iterate through unique 'name' values for the current 'ops' value
        for i, name in enumerate(unique_names):
            name_subset_df = subset_df[subset_df['name'] == name]
            plt.bar(name, name_subset_df['throughput'].iloc[0], label=str(name), color=name_colors[i])

        plt.xlabel('Queue')
        plt.ylabel('Throughput (op/s)')
        ax.yaxis.set_major_formatter(tkr.FuncFormatter(format))
        plt.title(f'{action} - {ops} operations')
        plt.legend(title='Ops', loc='upper right')
        plt.savefig(f'images/benchmark_{action}_{ops}.png', bbox_inches='tight')
        plt.clf()
