/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward some day, and you think this
 * stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire.AI;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import net.windward.Acquire.Units.GameMap;
import net.windward.Acquire.Units.HotelChain;
import net.windward.Acquire.Units.HotelStock;
import net.windward.Acquire.Units.MapTile;
import net.windward.Acquire.Units.Player;
import net.windward.Acquire.Units.PlayerTile;
import net.windward.Acquire.Units.SpecialPowers;
import net.windward.Acquire.Units.StockOwner;

/**
 * The sample C# AI. Start with this project but write your own code as this is a very simplistic implementation of the AI.
 */
public class MyPlayerBrain {
	// bugbug - put your team name here.

	private static String NAME = "BoilerMerctron";

	// bugbug - put your school name here. Must be 11 letters or less (ie use MIT, not Massachussets Institute of Technology).
	public static String SCHOOL = "Purdue CS";

	private static Logger log = Logger.getLogger(MyPlayerBrain.class);

	/**
	 * The name of the player.
	 */
	private String privateName;

	public final String getName() {
		return privateName;
	}

	private void setName(String value) {
		privateName = value;
	}

	private static final java.util.Random rand = new java.util.Random();

	public MyPlayerBrain(String name) {
		setName(!net.windward.Acquire.DotNetToJavaStringHelper.isNullOrEmpty(name) ? name : NAME);
	}

	/**
	 * The avatar of the player. Must be 32 x 32.
	 */
	public final byte[] getAvatar() {
		try {
			// open image
			InputStream stream = getClass().getResourceAsStream("/net/windward/Acquire/res/boilertron.png");

			byte[] avatar = new byte[stream.available()];
			stream.read(avatar, 0, avatar.length);
			return avatar;

		} catch (IOException e) {
			System.out.println("error reading image");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Called when the game starts, providing all info.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 */
	public void Setup(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
		// get your AI initialized here.
	}

	/**
	 * Asks if you want to play the CARD.DRAW_5_TILES or CARD.PLACE_4_TILES special power. This call will not be made
	 * if you have already played these cards.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @return CARD.NONE, CARD.PLACE_4_TILES, or CARD.DRAW_5_TILES.
	 */
	public int QuerySpecialPowerBeforeTurn(GameMap map, Player me, List<HotelChain> hotelChains,
	                                       List<Player> players) {
		
		// we randomly decide if we want to play a card.
		// We don't worry if we still have the card as the server will ignore trying to use a card twice.
		PlayerPlayTile tilePlay = new PlayerPlayTile();
		TileGoal tilePlacementGoal = chooseTilePlacement(map, me, hotelChains, players, tilePlay);
		if (me.getPowers().contains(SpecialPowers.CARD_PLACE_4_TILES) || (rand.nextInt(3) == 1 && tilePlacementGoal != TileGoal.NONE))
			return SpecialPowers.CARD_PLACE_4_TILES;
		if (rand.nextInt(3) == 1 && tilePlacementGoal == TileGoal.NONE)
			return SpecialPowers.CARD_DRAW_5_TILES;
		return SpecialPowers.CARD_NONE;
	}

	/**
	 * Return what tile to play when using the CARD.PLACE_4_TILES. This will be called for the first 3 tiles and is for
	 * placement only. Any merges due to this will be resolved before the next card is played. For the 4th tile,
	 * QueryTileAndPurchase will be called.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @return The tile(s) to play and the stock to purchase (and trade if CARD.TRADE_2_STOCK is played).
	 */
	public PlayerPlayTile QueryTileOnly(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {


		PlayerPlayTile playTile = new PlayerPlayTile();
		chooseTilePlacement(map, me, hotelChains, players, playTile);
		// we select a tile at random from our set
//		playTile.tile = me.getTiles().size() == 0 ? null : me.getTiles().get(rand.nextInt(me.getTiles().size()));
//		// we grab a random available hotel as the created hotel in case this tile creates a hotel
//		for (HotelChain hotel : hotelChains)
//			if (! hotel.isActive()) {
//				playTile.createdHotel = hotel;
//				break;
//			}
//		// We grab an existing hotel at random in case this tile merges multiple chains.
//		// note - the surviror may not be one of the hotels merged (this is a very stupid AI)!
//		for (HotelChain hotel : hotelChains)
//			if (hotel.isActive()) {
//				playTile.mergeSurvivor = hotel;
//				break;
//			}
		return playTile;
	}
	
	int[][] adjacentIndices = { {0, 1}, {1, 0}, {0, -1}, {-1, 0} };
	public boolean hasAdjacentTileOfType(GameMap map, int x, int y, int tileType) {
		for(int[] adjacentIndex: adjacentIndices) {
			int adjX = adjacentIndex[0];
			int adjY = adjacentIndex[1];
			if( adjX >= 0 && adjX < map.getWidth() && adjY >= 0 && adjY < map.getHeight() ) {
				if( map.getTiles(adjX, adjY).getType() == tileType ) {
					return true;
				}
			}
		}
		return false;
	}
	public List<MapTile> getAdjacentTilesOfType(GameMap map, int x, int y, int tileType) {
		List<MapTile> tiles = new ArrayList<MapTile>();
		for(int[] adjacentIndex: adjacentIndices) {
			int adjX = adjacentIndex[0];
			int adjY = adjacentIndex[1];
			if( adjX >= 0 && adjX < map.getWidth() && adjY >= 0 && adjY < map.getHeight() ) {
				if( map.getTiles(adjX, adjY).getType() == tileType ) {
					tiles.add(map.getTiles(adjX, adjY));
				}
			}
		}
		return tiles;
	}
	
	public static enum TileGoal { CREATE_COMPANY, MERGE, EXTEND, NONE };
	
	class ChainAndStocks {
		HotelChain chain;
		int stockAmount;
		public ChainAndStocks(HotelChain chain, int stockAmount) {
			this.chain = chain;
			this.stockAmount = stockAmount;
		}
		public HotelChain getChain() {
			return chain;
		}
		public void setChain(HotelChain chain) {
			this.chain = chain;
		}
		public int getStockAmount() {
			return stockAmount;
		}
		public void setStockAmount(int stockAmount) {
			this.stockAmount = stockAmount;
		}
	}
	
	public boolean isAStockOwner(Player player, List<StockOwner> owners) {
		for(StockOwner owner: owners) {
			if( owner.getOwner().getName().equals(player.getName()) ) {
				return true;
			}
		}
		return false;
	}
	
	public HotelChain chooseMergeSuccessor(List<HotelChain> chains, Player me) {
		List<HotelChain> chainsThatCanWin = new ArrayList<HotelChain>();
		int maxChainLength = 0;
		for(HotelChain chain: chains) {
			if( chain.getNumTiles() > maxChainLength ) {
				chainsThatCanWin.clear();
				maxChainLength = chain.getNumTiles();
			}
			if( chain.getNumTiles() >= maxChainLength ) {
				chainsThatCanWin.add(chain);
			}
		}
		List<ChainAndStocks> myStockOwnerships = new ArrayList<ChainAndStocks>();
		for(HotelChain chain: chainsThatCanWin) {
			StockOwner maxOwner = null;
			for( StockOwner owner: chain.getOwners()) {
				if( maxOwner == null || owner.getNumShares() > maxOwner.getNumShares() ) {
					maxOwner = owner;
				}
			}
			if( maxOwner != null && maxOwner.getOwner().getName().equals(me.getName()) ) {
				myStockOwnerships.add(new ChainAndStocks(chain, maxOwner.getNumShares()));
			}
		}
		Collections.sort(myStockOwnerships, new Comparator<ChainAndStocks>() {
			@Override
			public int compare(ChainAndStocks o1, ChainAndStocks o2) {
				return -new Integer(o1.getStockAmount()).compareTo(new Integer(o2.getStockAmount()));
			}
		});
		if( myStockOwnerships.size() > 0 ) {
			return myStockOwnerships.get(0).getChain();
		}
		return null;
	}
	
	public boolean isAGoodMerge(PlayerTile mergerTile, GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
		List<HotelChain> mergingChains = new ArrayList<HotelChain>();
		for(MapTile tile: getAdjacentTilesOfType(map, mergerTile.getX(), mergerTile.getY(), MapTile.TYPE_HOTEL)) {
			mergingChains.add(tile.getHotel());
		}
		return chooseMergeSuccessor(mergingChains, me) != null;
	}
	
	public HotelChain chooseBasicMerge(PlayerTile mergerTile, GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
		List<HotelChain> mergingChains = new ArrayList<HotelChain>();
		for(MapTile tile: getAdjacentTilesOfType(map, mergerTile.getX(), mergerTile.getY(), MapTile.TYPE_HOTEL)) {
			mergingChains.add(tile.getHotel());
		}
		return chooseMergeSuccessor(mergingChains, me);
	}

	/**
	 * Return what tile(s) to play and what stock(s) to purchase. At this point merges have not yet been processed.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @return The tile(s) to play and the stock to purchase (and trade if CARD.TRADE_2_STOCK is played).
	 */
	public PlayerTurn QueryTileAndPurchase(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
		PlayerTurn turn = new PlayerTurn();
		
		chooseTilePlacement(map, me, hotelChains, players, turn);

		// purchase random number of shares from random hotels.
		// note - This can try to purchase a hotel not on the board (this is a very stupid AI)!


		ArrayList<Integer> activeChains = new ArrayList<Integer>();
		int totalTiles = 0;
		int safeBonus = 0;
		int maxPrice = 0;
		for (int i = 0; i < hotelChains.size(); i++) {
			if (hotelChains.get(i).isActive()) {
				activeChains.add(i);
				totalTiles += hotelChains.get(i).getNumTiles();
				if (hotelChains.get(i).getStockPrice() > maxPrice) {
					maxPrice = hotelChains.get(i).getStockPrice();
				}
			}

		}

		int optimalStockIndex = 0;
		int optimalStockScore = 0;
		String optimalStockName = "";

		for (int j = 0; j < activeChains.size(); j++) {
			int chainLength = hotelChains.get(activeChains.get(j)).getNumTiles();
			int currentPrice = hotelChains.get(activeChains.get(j)).getStockPrice();
			int startPrice = hotelChains.get(activeChains.get(j)).getStartPrice();
			if (hotelChains.get(activeChains.get(j)).isSafe()) safeBonus = 20;
			else safeBonus = 0;

			int currScore = Math.round((chainLength/totalTiles)*100) + Math.round((startPrice/400)*200) + safeBonus + Math.round((currentPrice/maxPrice)*100);
			if (optimalStockScore < currScore && hotelChains.get(activeChains.get(j)).getNumAvailableShares() > 1) {
				optimalStockName = hotelChains.get(activeChains.get(j)).getName();
				optimalStockScore = currScore;
				optimalStockIndex = activeChains.get(j);
			}
		}

		System.out.println("Optimal Stock: " + optimalStockName + ", " + optimalStockScore);
		turn.getBuy().add(new HotelStock(hotelChains.get(optimalStockIndex), 2));

		optimalStockIndex = 0;
		optimalStockScore = 0;
		optimalStockName = "";

		for (int j = 0; j < activeChains.size(); j++) {
			int chainLength = hotelChains.get(activeChains.get(j)).getNumTiles();
			int currentPrice = hotelChains.get(activeChains.get(j)).getStockPrice();
			int startPrice = hotelChains.get(activeChains.get(j)).getStartPrice();
			if (hotelChains.get(activeChains.get(j)).isSafe()) safeBonus = 100;
			else safeBonus = 0;

			int currScore = Math.round((chainLength/totalTiles)*150) + Math.round((startPrice/400)*100) + safeBonus + Math.round((maxPrice/currentPrice)*100);
			if (optimalStockScore < currScore && hotelChains.get(activeChains.get(j)).getNumAvailableShares() > 0) {
				optimalStockName = hotelChains.get(activeChains.get(j)).getName();
				optimalStockScore = currScore;
				optimalStockIndex = activeChains.get(j);
			}
		}

		System.out.println("Optimal Stock: " + optimalStockName + ", " + optimalStockScore);
		turn.getBuy().add(new HotelStock(hotelChains.get(optimalStockIndex), 1));


		if (rand.nextInt(20) != 1)
			return turn;

		// randomly occasionally play one of the cards
		// We don't worry if we still have the card as the server will ignore trying to use a card twice.
		switch (rand.nextInt(3)) {
			case 0:
				turn.setCard(SpecialPowers.CARD_BUY_5_STOCK);

				for (int k = 0; k < 5; k ++) {
					optimalStockIndex = 0;
					optimalStockScore = 0;
					optimalStockName = "";

					for (int j = 0; j < activeChains.size(); j++) {
						int chainLength = hotelChains.get(activeChains.get(j)).getNumTiles();
						int currentPrice = hotelChains.get(activeChains.get(j)).getStockPrice();
						int startPrice = hotelChains.get(activeChains.get(j)).getStartPrice();
						if (hotelChains.get(activeChains.get(j)).isSafe()) safeBonus = 100;
						else safeBonus = 0;

						int currScore = Math.round((chainLength/totalTiles)*150) + Math.round((startPrice/400)*100) + safeBonus + Math.round((currentPrice/maxPrice)*100);
						if (optimalStockScore < currScore && hotelChains.get(activeChains.get(j)).getNumAvailableShares() > 0) {
							optimalStockName = hotelChains.get(activeChains.get(j)).getName();
							optimalStockScore = currScore;
							optimalStockIndex = activeChains.get(j);
						}
					}

					System.out.println("Optimal Stock: " + optimalStockName + ", " + optimalStockScore);
					turn.getBuy().add(new HotelStock(hotelChains.get(optimalStockIndex), 1));
				}

				return turn;
			case 1:
				turn.setCard(SpecialPowers.CARD_FREE_3_STOCK);
				return turn;
			default:
				if (me.getStock().size() > 0) {
					turn.setCard(SpecialPowers.CARD_TRADE_2_STOCK);

					optimalStockIndex = 0;
					optimalStockScore = 0;
					optimalStockName = "";

					for (int j = 0; j < activeChains.size(); j++) {
						int chainLength = hotelChains.get(activeChains.get(j)).getNumTiles();
						int currentPrice = hotelChains.get(activeChains.get(j)).getStockPrice();
						int startPrice = hotelChains.get(activeChains.get(j)).getStartPrice();
						if (hotelChains.get(activeChains.get(j)).isSafe()) safeBonus = 100;
						else safeBonus = 0;

						int currScore = Math.round((chainLength/totalTiles)*150) + Math.round((startPrice/400)*100) + safeBonus + Math.round((currentPrice/maxPrice)*100);
						if (optimalStockScore < currScore && hotelChains.get(activeChains.get(j)).getNumAvailableShares() > 0) {
							optimalStockName = hotelChains.get(activeChains.get(j)).getName();
							optimalStockScore = currScore;
							optimalStockIndex = activeChains.get(j);
						}
					}

					System.out.println("Optimal Stock: " + optimalStockName + ", " + optimalStockScore);

					turn.getTrade().add(new PlayerTurn.TradeStock(me.getStock().get(rand.nextInt(me.getStock().size())).getChain(),
							hotelChains.get(optimalStockIndex)));
				}
				return turn;
		}
	}

	private TileGoal chooseTilePlacement(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players,
			PlayerPlayTile turn) {
		TileGoal tileGoal = TileGoal.NONE; // allows us to look at what our tile strategy was
		// first, look for a place to form a new company
		boolean atLeastOneInactiveCompany = false;
		for(HotelChain hotelChain: hotelChains) {
			if( !hotelChain.isActive() ) {
				atLeastOneInactiveCompany = true;
			}
		}
		for(PlayerTile tile: me.getTiles()) {
			if( map.IsTileUnplayable(tile) ) {
				// do not consider an unplayable tile
				continue;
			} else if( atLeastOneInactiveCompany && hasAdjacentTileOfType(map, tile.getX(), tile.getY(), MapTile.TYPE_SINGLE) ) {
				turn.tile = tile;
				tileGoal = TileGoal.CREATE_COMPANY;
				break;
			}
		}
		// second, look for a place to make a beneficial merge
		if( turn.tile == null ) {
			for(PlayerTile tile: me.getTiles()) {
				if( map.IsTileUnplayable(tile) ) {
					// do not consider an unplayable tile
					continue;
				} else if( hasAdjacentTileOfType(map, tile.getX(), tile.getY(), MapTile.TYPE_HOTEL) ) {
					List<MapTile> adjacentHotelTiles = getAdjacentTilesOfType(map, tile.getX(), tile.getY(), MapTile.TYPE_HOTEL);
					boolean allSame = true;
					HotelChain allChain = null;
					List<HotelChain> mergingChains = new ArrayList<HotelChain>();
					for(MapTile mapTile: adjacentHotelTiles) {
						if( allChain == null || allChain == mapTile.getHotel() ) {
							allChain = mapTile.getHotel();
						} else {
							mergingChains.add(mapTile.getHotel());
							allSame = false;
						}
					}
					for(HotelChain chain: mergingChains) {
						if( chain.isSafe() ) {
							// dont do stuff
							continue;
						}
					}
					
					if( !allSame && adjacentHotelTiles.size() > 1 && isAGoodMerge(tile, map, me, hotelChains, players) ) {
						// if its a merge, determine if we benefit
						turn.tile = tile;
						turn.mergeSurvivor = chooseBasicMerge(tile, map, me, hotelChains, players);
						tileGoal = TileGoal.MERGE;
						break;
					}
				}
			}
		}
		// third, look for a place to extend one of our companies
		if( turn.tile == null ) {
			for(PlayerTile tile: me.getTiles()) {
				if( map.IsTileUnplayable(tile) ) {
					// do not consider an unplayable tile
					continue;
				} else if( hasAdjacentTileOfType(map, tile.getX(), tile.getY(), MapTile.TYPE_HOTEL) ) {
					List<MapTile> adjacentHotelTiles = getAdjacentTilesOfType(map, tile.getX(), tile.getY(), MapTile.TYPE_HOTEL);
					boolean allSame = true;
					HotelChain allChain = null;
					for(MapTile mapTile: adjacentHotelTiles) {
						if( allChain == null || allChain == mapTile.getHotel() ) {
							allChain = mapTile.getHotel();
						} else {
							allSame = false;
							break;
						}
					}
					
					if( allSame && (isAStockOwner(me, allChain.getFirstMajorityOwners()) || isAStockOwner(me, allChain.getSecondMajorityOwners())) ) {
						// if its a merge
						turn.tile = tile;
						tileGoal = TileGoal.EXTEND;
						break;
					}
				}
			}
		}
		if( turn.tile == null ) {
			turn.tile = me.getTiles().size() == 0 ? null : me.getTiles().get(rand.nextInt(me.getTiles().size()));
		}
		// we grab a random available hotel as the created hotel in case this tile creates a hotel
		List<HotelChain> chainsByStartingPrice = new ArrayList<HotelChain>(hotelChains);// copy constructor
		Collections.sort(chainsByStartingPrice, new Comparator<HotelChain>() {
			@Override
			public int compare(HotelChain o1, HotelChain o2) {
				return -new Integer(o1.getStartPrice()).compareTo(o2.getStartPrice());
			}
		});
		for (HotelChain hotel : chainsByStartingPrice)
			if (!hotel.isActive()) {
				turn.createdHotel = hotel;
				break;
			}
		return tileGoal;
	}

	/**
	 * Ask the AI what they want to do with their merged stock. If a merge is for 3+ chains, this will get called once
	 * per removed chain.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @param survivor The hotel that survived the merge.
	 * @param defunct The hotel that is now defunct.
	 * @return What you want to do with the stock.
	 */
	public PlayerMerge QueryMergeStock(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players,
	                                   HotelChain survivor, HotelChain defunct) {


		HotelStock myStock = null;
		for (HotelStock stock : me.getStock())
			if (stock.getChain() == defunct) {
				myStock = stock;
				break;
			}
		// we sell, keep, & trade 1/3 of our shares in the defunct hotel
		return new PlayerMerge(0, 0, myStock.getNumShares());
	}
}