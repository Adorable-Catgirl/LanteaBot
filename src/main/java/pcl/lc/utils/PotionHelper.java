package pcl.lc.utils;

import gcardone.junidecode.App;
import org.joda.time.DateTime;
import pcl.lc.irc.hooks.DrinkPotion;
import pcl.lc.irc.hooks.Inventory;

import java.util.ArrayList;
import java.util.HashMap;

public class PotionHelper {
	/**
	 * @return String[] Returns three values: consistency, appearance and "" or "new" (whether potion has been generated already today)
	 */
	public static PotionEntry getRandomPotion() {
		int coli = getRandomAppearanceIndex();
		int coni = getRandomConsistencyIndex();
		AppearanceEntry app = getAppearance(coli);
		AppearanceEntry con = getConsistency(coni);
		return new PotionEntry(con, app, !PotionHelper.combinationHasEffect(con, app));
	}

	public static void tryResetPotionList() {
		if (DrinkPotion.day_of_potioning == null || DrinkPotion.day_of_potioning.equals("") || DateTime.parse(DrinkPotion.day_of_potioning).isBefore(DateTime.now())) {
			resetPotionList();
		}
	}

	public static void resetPotionList() {
		System.out.println("Resetting potion list!");
		DrinkPotion.potions = new HashMap<>();
		DrinkPotion.day_of_potioning = DateTime.now().plusDays(DrinkPotion.daysPotionsLast).toString("yyyy-MM-dd");
	}

	public static boolean combinationHasEffect(AppearanceEntry consistency, AppearanceEntry appearance) {
		tryResetPotionList();
		String key = getConsistencyIndexByName(consistency.getName()) + "," + getAppearanceIndexByName(appearance.getName());
		if (DrinkPotion.potions.containsKey(key))
			return true;
		return false;
	}

	public static void setCombinationEffect(AppearanceEntry consistency, AppearanceEntry appearance, EffectEntry effect) {
		String key = getConsistencyIndexByName(consistency.getName()) + "," + getAppearanceIndexByName(appearance.getName());
		DrinkPotion.potions.put(key, effect);
	}

	public static EffectEntry getCombinationEffect(AppearanceEntry consistency, AppearanceEntry appearance) {
		String key = getConsistencyIndexByName(consistency.getName()) + "," + getAppearanceIndexByName(appearance.getName());
		return DrinkPotion.potions.get(key);
	}

	public static AppearanceEntry findAppearanceInString(ArrayList<String> string) {
		return findAppearanceInString(String.join(" ", string));
	}

	public static AppearanceEntry findAppearanceInString(String string) {
		string = Helper.reverseString(string).toLowerCase();
		System.out.println(string);
		for (AppearanceEntry c : DrinkPotion.appearanceEntries) {
			if (string.contains(c.Name))
				return c;
		}
		return null;
	}

	public static AppearanceEntry findConsistencyInString(ArrayList<String> string) {
		return findConsistencyInString(String.join(" ", string));
	}

	public static AppearanceEntry findConsistencyInString(String string) {
		string = Helper.reverseString(string).toLowerCase();
		System.out.println(string);
		for (AppearanceEntry c : DrinkPotion.consistencies) {
			if (string.contains(c.getName().toLowerCase()))
				return c;
		}
		return null;
	}

	public static int getRandomAppearanceIndex() {
		return Helper.getRandomInt(0, DrinkPotion.appearanceEntries.size() - 1);
	}

	public static AppearanceEntry getAppearance() {
		return getAppearance(getRandomAppearanceIndex());
	}

	public static AppearanceEntry getAppearance(int index) {
		return DrinkPotion.appearanceEntries.get(index);
	}

	public static int getRandomConsistencyIndex() {
		return Helper.getRandomInt(0, DrinkPotion.consistencies.size() - 1);
	}

	public static AppearanceEntry getConsistency() {
		return getConsistency(getRandomConsistencyIndex());
	}

	public static AppearanceEntry getConsistency(int index) {
		return DrinkPotion.consistencies.get(index);
	}

	public static int getRandomLimitIndex() {
		return Helper.getRandomInt(0, DrinkPotion.limits.size() - 1);
	}

	public static String getLimit() {
		return getLimit(getRandomLimitIndex());
	}

	public static String getLimit(int index) {
		return DrinkPotion.limits.get(index);
	}

	public static int getAppearanceIndexByName(String name) {
		for (int i = 0; i < DrinkPotion.appearanceEntries.size(); i++) {
			AppearanceEntry e = DrinkPotion.appearanceEntries.get(i);
			if (e.getName().toLowerCase().equals(name.toLowerCase()))
				return i;
		}
		return -1;
	}

	public static int getConsistencyIndexByName(String name) {
		for (int i = 0; i < DrinkPotion.consistencies.size(); i++) {
			AppearanceEntry e = DrinkPotion.consistencies.get(i);
			if (e.getName().toLowerCase().equals(name.toLowerCase()))
				return i;
		}
		return -1;
	}

	public static String replaceParamsInEffectString(String effect, String nickUser) {
		effect = replaceParamsInEffectString(effect);
		return effect.replaceAll("\\{user}", nickUser);
	}

	public static String replaceParamsInEffectString(String effect, String nickUser, String targetUser) {
		effect = replaceParamsInEffectString(effect);
		return effect.replaceAll("\\{user}", nickUser)
				.replaceAll("\\{target}", targetUser);
	}

	public static String replaceParamsInEffectString(String effect) {
		String replace_appearance = PotionHelper.getAppearance().getName();
		String replace_appearance_prefix = PotionHelper.getAppearance().getName(true);
		String turn_appearance = PotionHelper.getAppearance().turnsTo();
		String replace_consistency = PotionHelper.getConsistency().getName();
		String replace_consistency_prefix = PotionHelper.getConsistency().getName(true);
		String limit = PotionHelper.getLimit();

		return effect
                .replace("{item}", Inventory.getRandomItem().getNameRaw())
                .replace("{appearance}", replace_appearance)
				.replace("{appearance_p}", replace_appearance_prefix)
				.replace("{turn_appearance}", turn_appearance)
				.replace("{consistency}", replace_consistency)
				.replace("{consistency_p}", replace_consistency_prefix)
				.replace("{transformation}", Helper.getRandomTransformation(true, false, false))
				.replace("{transformation_p}", Helper.getRandomTransformation(true, true, false))
				.replace("{transformation2}", Helper.getRandomTransformation(true, false, false))
				.replace("{transformations}", Helper.getRandomTransformation(true, true, true))
				.replace("{transformations_p}", Helper.getRandomTransformation(true, false, true))
				.replace("{transformations2}", Helper.getRandomTransformation(true, false, true))
				.replace("{limit}", limit);
	}
}