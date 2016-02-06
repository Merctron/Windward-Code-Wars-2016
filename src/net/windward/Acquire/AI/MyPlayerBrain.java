/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward some day, and you think this
 * stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire.AI;

import net.windward.Acquire.Units.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The sample C# AI. Start with this project but write your own code as this is a very simplistic implementation of the AI.
 */
public class MyPlayerBrain {
	// bugbug - put your team name here.
	private static String NAME = "BoilerTron";

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
			InputStream stream = getClass().getResourceAsStream("/net/windward/Acquire/res/MyAvatar.png");

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
		if (rand.nextInt(30) == 1)
			return SpecialPowers.CARD_DRAW_5_TILES;
		if (rand.nextInt(30) == 1)
			return SpecialPowers.CARD_PLACE_4_TILES;
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
		// we select a tile at random from our set
		playTile.tile = me.getTiles().size() == 0 ? null : me.getTiles().get(rand.nextInt(me.getTiles().size()));
		// we grab a random available hotel as the created hotel in case this tile creates a hotel
		for (HotelChain hotel : hotelChains)
			if (! hotel.isActive()) {
				playTile.createdHotel = hotel;
				break;
			}
		// We grab an existing hotel at random in case this tile merges multiple chains.
		// note - the surviror may not be one of the hotels merged (this is a very stupid AI)!
		for (HotelChain hotel : hotelChains)
			if (hotel.isActive()) {
				playTile.mergeSurvivor = hotel;
				break;
			}
		return playTile;
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
		// we select a tile at random from our set
		turn.tile = me.getTiles().size() == 0 ? null : me.getTiles().get(rand.nextInt(me.getTiles().size()));
		// we grab a random available hotel as the created hotel in case this tile creates a hotel
		for (HotelChain hotel : hotelChains)
			if (! hotel.isActive()) {
				turn.createdHotel = hotel;
				break;
			}
		// We grab an existing hotel at random in case this tile merges multiple chains.
		// note - the surviror may not be one of the hotels merged (this is a very stupid AI)!
		for (HotelChain hotel : hotelChains)
			if (hotel.isActive()) {
				turn.mergeSurvivor = hotel;
				break;
			}

		// purchase random number of shares from random hotels.
		// note - This can try to purchase a hotel not on the board (this is a very stupid AI)!
		turn.getBuy().add(new HotelStock(hotelChains.get(rand.nextInt(hotelChains.size())), 1 + rand.nextInt(3)));
		turn.getBuy().add(new HotelStock(hotelChains.get(rand.nextInt(hotelChains.size())), 1 + rand.nextInt(3)));

		if (rand.nextInt(20) != 1)
			return turn;

		// randomly occasionally play one of the cards
		// We don't worry if we still have the card as the server will ignore trying to use a card twice.
		switch (rand.nextInt(3)) {
			case 0:
				turn.setCard(SpecialPowers.CARD_BUY_5_STOCK);
				turn.getBuy().add(new HotelStock(hotelChains.get(rand.nextInt(hotelChains.size())), 3));
				return turn;
			case 1:
				turn.setCard(SpecialPowers.CARD_FREE_3_STOCK);
				return turn;
			default:
				if (me.getStock().size() > 0) {
					turn.setCard(SpecialPowers.CARD_TRADE_2_STOCK);
					turn.getTrade().add(new PlayerTurn.TradeStock(me.getStock().get(rand.nextInt(me.getStock().size())).getChain(),
							hotelChains.get(rand.nextInt(hotelChains.size()))));
				}
				return turn;
		}
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
		return new PlayerMerge(myStock.getNumShares() / 3, myStock.getNumShares() / 3, (myStock.getNumShares() + 2) / 3);
	}
}