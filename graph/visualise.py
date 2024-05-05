from jaal import Jaal
import pandas as pd

edge_df = pd.read_csv(
    "app/src/main/resources/facebook-wosn-links/out.facebook-wosn-links",
    sep=" ",
    skiprows=2,
    names=["from", "to", "weight", "ts1"],
).drop(["weight", "ts1"], axis=1)[:92]

# init Jaal and run server
Jaal(edge_df).plot()
