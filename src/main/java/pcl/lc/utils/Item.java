package pcl.lc.utils;

import com.google.api.client.util.DateTime;
import pcl.lc.irc.hooks.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Item class
 * Created by Forecaster on 12/03/2017 for the LanteaBot project.
 */
public class Item {
	private int id;
	private String name;
	private int uses_left;
	private boolean is_favourite;
	private String added_by;
	private int added;

	public Item(String name) throws Exception {
		PreparedStatement statement = Database.getPreparedStatement("getItemByName");
		statement.setString(1, name);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			this.id = resultSet.getInt(1);
			this.name = resultSet.getString(2);
			this.uses_left = resultSet.getInt(3);
			this.is_favourite = resultSet.getBoolean(4);
			this.added_by = resultSet.getString(5);
			this.added = resultSet.getInt(6);
		} else {
			throw new Exception("No item '" + name + "' found");
		}
	}

	public Item(int id) {
		try {
			PreparedStatement statement = Database.getPreparedStatement("getItem");
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				this.id = resultSet.getInt(1);
				this.name = resultSet.getString(2);
				this.uses_left = resultSet.getInt(3);
				this.is_favourite = resultSet.getBoolean(4);
				this.added_by = resultSet.getString(5);
				this.added = resultSet.getInt(6);
			} else {
				throw new Exception("No item '" + name + "' found");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Item(int id, String name, int uses_left, boolean is_favourite, String added_by, int added) {
		this.id = id;
		this.name = name;
		this.uses_left = uses_left;
		this.is_favourite = is_favourite;
		this.added_by = added_by;
		this.added = added;
	}

	/**
	 * Applies massive damage to item, almost guaranteed to destroy it.
	 * Calls destroy with includeLeadingComma = true, capitalizeFirstWord = false, includeEndPunctuation = true
	 * @return The dust string from the destroyed item
	 */
	public String destroy() {
		return destroy(true, false, true);
	}

	/**
	 * Applies massive damage to item, almost guaranteed to destroy it.
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return The dust string from the destroyed item
	 */
	public String destroy(boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		return damage(999, includeLeadingComma, capitalizeFirstWord, includeEndPunctuation);
	}

	/**
	 * Applies one (1) damage to the item, defaults to includeLeadingComma:true, capitalizeFirstWord:false, includeEndPunctuation:false
	 * @return String
	 */
	public String decrementUses() {
		return damage(1);
	}

	/**
	 * Applies one (1) damage to the item
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return String
	 */
	public String decrementUses(boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		return damage(1, includeLeadingComma, capitalizeFirstWord, includeEndPunctuation);
	}

	/**
	 * Applies one (1) damage to the item
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return String
	 */
	public String damage(boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		return damage(1, includeLeadingComma, capitalizeFirstWord, includeEndPunctuation);
	}

	/**
	 * Applies damage to the item, defaults to includeLeadingComma:true, capitalizeFirstWord:false, includeEndPunctuation:false
	 * @param damage Amount of damage to apply
	 * @return String
	 */
	public String damage(int damage) {
		return damage(damage, true, false, false);
	}

	/**
	 * Applies damage to the item. If result is 0 or less item is destroyed unless it's preserved
	 * Returns the 'dust' string to append if the item was destroyed, empty string otherwise. 'Dust' string should be appended at the end of the message to the channel/user
	 * @param damage Amount of damage to apply
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return String
	 */
	public String damage(int damage, boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		if (this.uses_left == -1)
			return "";
		this.uses_left -= damage;
		if (this.uses_left <= 0) {
			int result = Inventory.removeItem(this.id);
			if (result == 0) {
				String sentence = Inventory.getItemBreakString(Inventory.fixItemName(this.name, true), includeEndPunctuation);
				if (capitalizeFirstWord)
					sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
				return (includeLeadingComma ? ", " : "") + sentence + ".";
			}
			else
				System.out.println("Error removing item (" + result + ")");
		} else {
			try {
				PreparedStatement statement = Database.getPreparedStatement("setUses");
				statement.setInt(1, this.uses_left);
				statement.setInt(2, this.id);
				statement.executeUpdate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return "";
	}

	public void addUses(int uses) {
		this.uses_left += uses;
		try {
			PreparedStatement statement = Database.getPreparedStatement("setUses");
			statement.setInt(1, this.uses_left);
			statement.setInt(2, this.id);
			statement.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void incrementUses() {
		addUses(1);
	}

	public int removeItem() {
		return Inventory.removeItem(this.id);
	}

	public boolean preserve() {
		try {
			PreparedStatement preserveItem = Database.getPreparedStatement("preserveItem");
			preserveItem.setString(1, this.name);
			preserveItem.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean unPpreserve() {
		try {
			PreparedStatement unPreserveItem = Database.getPreparedStatement("unPreserveItem");
			unPreserveItem.setString(1, this.name);
			unPreserveItem.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return getName(false);
	}

	public String getName(boolean sort_out_prefixes) {
		if (this.name == null)
			return "null";
		try {
			return Inventory.fixItemName(this.name, sort_out_prefixes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.name;
	}

	public Item setName(String new_name) {
		this.name = new_name;
		return this;
	}

	public String getNameWithoutPrefix() {
		if (this.name == null)
			return "null";
		try {
			return Inventory.fixItemName(this.name, true, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.name;
	}

	public String getNameRaw() {
		return this.name;
	}

	public String getAdded_by() {
		return this.added_by;
	}

	public int getAddedRaw() {
		return this.added;
	}

	public DateTime getAdded() {
		return new DateTime(this.added);
	}

	public int getUsesLeft() {
		return this.uses_left;
	}

	public String getUsesLeftVague() {
		return Inventory.getUsesIndicator(this.uses_left);
	}

	public boolean isFavourite() {
		return this.is_favourite;
	}

	public int[] getGenericRoll()
	{
		return getGenericRoll(0);
	}

	public int[] getGenericRoll(int bonus)
	{
		return getGenericRoll(4, bonus);
	}

	public int[] getGenericRoll(int diceSize, int bonus)
	{
		return getGenericRoll(diceSize, bonus, 0);
	}

	public int[] getGenericRoll(int diceSize, int bonus, int minValue)
	{
		int[] result = new int[4];
		int diceRoll = Helper.rollDice(Math.max(1, (this.uses_left / 2)) + "d" + diceSize).getSum();
		result[0] = (bonus == Integer.MIN_VALUE ? 0 : Math.max(minValue, diceRoll + bonus)); //Result
		result[1] = diceRoll;
		result[2] = bonus;
		result[3] = minValue; //Minimum Value
		return result;
	}

	/**
	 * Default dice size 4
	 * @return int
	 */
	public int[] getDamage()
	{
		return getDamage(4);
	}

	public int[] getDamage(int diceSize)
	{
		return getDamage(diceSize, 0);
	}

	public int[] getDamage(int diceSize, int minDamage)
	{
		return getGenericRoll(diceSize, Helper.getOffensiveItemBonus(this), minDamage);
	}

	/**
	 * Default dice size 4
	 * @return int
	 */
	public int[] getDamageRecution()
	{
		return getDamageReduction(4);
	}

	public int[] getDamageReduction(int diceSize)
	{
		return getDamageReduction(diceSize, 0);
	}

	public int[] getDamageReduction(int diceSize, Integer minDamageReduction)
	{
		return getGenericRoll(diceSize, Helper.getDefensiveItemBonus(this), minDamageReduction);
	}

	/**
	 * Default dice size 4
	 * @return int
	 */
	public int[] getHealing()
	{
		return getHealing(4);
	}

	public int[] getHealing(int diceSize)
	{
		return getHealing(diceSize, 0);
	}

	public int[] getHealing(int diceSize, int minHealing)
	{
		return getGenericRoll(diceSize, Helper.getHealingItemBonus(this), minHealing);
	}

	/**
	 * "{damage} damage (Minimum|{diceRoll}+|-{bonus})"
	 * @param input int[] The result array from one of the get____Roll methods
	 * @return String
	 */
	public static String stringifyDamageResult(int[] input)
	{
		if (getResult(input) == 0)
			return "no damage" + getParenthesis(input);
		return getResult(input) + " damage" + getParenthesis(input);
	}

	/**
	 * "damage reduced by {reduction} (Minimum|{diceRoll}+-{bonus}"
	 * or
	 * "no damage reduction (Incapable|{diceRoll}+-{bonus})"
	 * @param input int[] The result array from one of the get____Roll methods
	 * @return String
	 */
	public static String stringifyDamageReductionResult(int[] input)
	{
		if (getResult(input) == 0)
			return "no damage reduction" + getParenthesis(input);
		return "damage reduced by " + getResult(input) + getParenthesis(input);
	}

	/**
	 * "gained {health} health (Minimum|{diceRoll}+-{bonus})"
	 * or
	 * "no health gained (Incapable|{diceRoll}+-{bonus})
	 * @param input int[] The result array from one of the get____Roll methods
	 * @return String
	 */
	public static String stringifyHealingResult(int[] input)
	{
		if (getResult(input) == 0)
			return "no health gained" + getParenthesis(input);
		return getResult(input) + " health gained" + getParenthesis(input);
	}

	public static int getResult(int[] input)
	{
		return input[0];
	}

	public static int getDiceRoll(int[] input)
	{
		return input[1];
	}

	public static int getBonus(int[] input)
	{
		return input[2];
	}

	public static int getMinValue(int[] input)
	{
		return input[3];
	}

	public static String getParenthesis(int[] input)
	{
		if (getBonus(input) == 0)
			return "";
		if (getBonus(input) == Integer.MIN_VALUE)
			return " (Incapable)";
		if (getResult(input) == getMinValue(input))
			return " (Minimum)";
		return " (" + getDiceRoll(input) + (getBonus(input) < 0 ? "" : "+") + getBonus(input) + ")";
	}
}
