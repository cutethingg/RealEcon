WIP
# RealEcon
📈 RealEcon Mod Documentation
Welcome to the official wiki for RealEcon, a dynamic, player-driven economy mod for Minecraft Forge. RealEcon transforms static server shops into a breathing commodities market using graph-theory mathematics, automated storefronts, and comprehensive ledger tracking.
📑 Table of Contents
General Concepts
Player Guide (Step-by-Step)
Admin Guide (Step-by-Step)
Commands & Permissions
Configuration Syntax
1. General Concepts
RealEcon operates on a "Value-Driven Barter Economy." Instead of relying on a virtual-only balance or a custom modded coin, the server administrator assigns hard-coded fiat values to existing Minecraft items (e.g., 1 Gold Nugget = $1).
From there, the mod's Global Market Manager tracks every player-to-player trade and uses an algorithm to dynamically calculate the dollar value of every other item traded on the server.
Core Items & Blocks
The Wagebox: An automated shop block (resembling a barrel) with a 27-slot inventory. It securely handles player-to-player item exchanges even when the owner is offline.
The Trade Ledger: A personal planning book used to create, save, and configure exact trade ratios (e.g., 64 Cobblestone for 1 Iron Ingot).
The Master Ledger: A comprehensive economic dashboard item featuring three tabs:
My Empire Stats: Tracks personal Net Worth, Trade Revenue, Shop Stock Value, and lists active Open Orders.
Global Shops: A server-wide directory of all active Wageboxes and their coordinates.
Global Market Index (GMI): A live, dynamically calculated stock market listing the current dollar value of items based on recent trading trends.



2. Player Guide (Step-by-Step)
How to Set Up a Shop (Wagebox)
Craft a Trade Ledger: Open it by right-clicking.
Create a Trade: * Place the item you want to receive (bounty) in the left slot (Green Checkmark).
Place the item you want to give (reward) in the right slot (Red Fire).
Click "+ Add". The trade will appear in your Active Trades list on the right page. You can configure multiple trades per ledger.
Place a Wagebox: Put down the Wagebox block where customers can access it.
Apply the Trades: Insert your configured Trade Ledger into the Wagebox to set its offers.
Stock the Inventory: Open the Wagebox's 27-slot physical inventory and fill it with the items you are selling.
Note: The Wagebox will automatically stop offering a trade if it runs out of reward items or if it runs out of physical space to hold incoming payments.
How to Buy from a Wagebox
Walk up to any player's Wagebox and right-click to open the Villager-style trading menu.
Ensure you have the required payment items in your inventory.
Click the trade output. The Wagebox will securely extract your payment, hand you your purchased items, and log the receipt to the server's market brain.
How to Track the Market
Open your Master Ledger.
Navigate to the Global Market Index (Page 3).
View the dynamically calculated prices of items across the server. At the bottom of the page, a countdown timer will show exactly how many trades are left across the server before the next market price shift occurs.







3. Admin Guide (Step-by-Step)
Setting up the Server Economy
Define the Anchor: Decide what standard Minecraft item represents your base currency (e.g., minecraft:gold_nugget). This item will be hardcoded to exactly $1.00 to ground the math algorithm.
Define Supplemental Currency: In the config, you can add standard values to larger denominations (e.g., Gold Ingots = $9.00) so the market graph has multiple points of stability.
Tune the Volatility: Use the config to determine how fast prices shift. Set the marketUpdateTrades to a low number (like 10) for a highly volatile, fast-moving stock market, or a high number (like 1000) for a slow, stable economy.
Choose the Math: Decide between WMA (Weighted Moving Average—recent trades heavily impact current prices) or AVERAGE (historical average—prices move very slowly based on all-time data).
4. Commands & Permissions
Player Commands (Permission Level: 0)
/bal
Description: Calculates the player's total net worth and prints it to their chat.
/baltop
Description: Scans all currently online players and prints a Top 10 Wealth Leaderboard to the chat.
/econadmin
Description: Main RealEcon command help.
Admin Commands (Permission Level: 2+)
/econadmin force_market_update
Description: Bypasses the standard trade countdown and forces the Global Market Manager to immediately recalculate the Global Price Index and push the update to all Master Ledgers. Useful for testing or forcefully stabilizing the market.





5. Configuration Syntax
The RealEcon configuration is located in the server config folder (realecon-server.toml).
[Currency]
Defines the hard-coded fiat values of specific items. These act as the unbreakable anchors for the dynamic market graph.
Rule: All values must be whole integers. No decimals.
Syntax 1 (Specific Item): "modid:item_name,value"
Syntax 2 (Tag Matching): "#modid:tag_name,value"
Syntax 3 (Debt Items): Negative values are supported (e.g., "minecraft:dirt,-1").
Example:

Ini, TOML


currency_list = [
  "minecraft:gold_nugget,1",
  "minecraft:gold_ingot,9",
  "minecraft:gold_block,81"
]


[GlobalMarket]
Controls the algorithms governing the dynamic Global Market Index.
anchorCurrency (String)
Default: "minecraft:gold_nugget"
Description: The item used as the $1.00 baseline for calculating all graph edges.
marketUpdateTrades (Integer)
Range: 1 to 100,000
Default: 10
Description: How many player-to-player trades must occur before the server runs the Dijkstra pathfinding algorithm to calculate new market prices.
mathMode (Enum)
Options: WMA or AVERAGE
Default: WMA
Description: WMA adjusts prices faster based on recent trades. AVERAGE counts all historical trades equally.
wmaWeight (Double)
Range: 0.01 to 1.0
Default: 0.2
Description: If using WMA, determines the weight of the newest trade data versus historical data. (0.2 = 20% impact from new trades, 80% impact from history).
