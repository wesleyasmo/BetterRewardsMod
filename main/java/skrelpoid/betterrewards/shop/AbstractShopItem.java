package skrelpoid.betterrewards.shop;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.InputHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;

// Mostly copied from StoreRelic
public abstract class AbstractShopItem {

	private static final float GOLD_OFFSET_X = -56.0F * Settings.scale;
	private static final float GOLD_OFFSET_Y = -100.0F * Settings.scale;
	private static final float PRICE_OFFSET_X = 14.0F * Settings.scale;
	private static final float PRICE_OFFSET_Y = -62.0F * Settings.scale;
	private static final float GOLD_IMG_WIDTH = ImageMaster.UI_GOLD.getWidth() * Settings.scale;
	protected Texture texture;
	protected ArrayList<PowerTip> tips;
	public int price;
	protected float x;
	protected float y;
	protected float scale;
	protected boolean isVisible;
	protected Hitbox hb;
	protected ShopScreen shopScreen;
	// TODO add compatibility with membership card and the courier (price
	// reduction)

	// Texture should be 128 by 128
	public AbstractShopItem(ShopScreen shopScreen, String texPath, String name, String description, int cost, float x,
			float y) {
		this.shopScreen = shopScreen;
		texture = new Texture(texPath);
		tips = new ArrayList<PowerTip>();
		tips.add(new PowerTip(name, description));
		this.price = cost;
		isVisible = true;
		hb = new Hitbox(120.0f * Settings.scale, 120.0f * Settings.scale);
		this.x = x * Settings.scale;
		this.y = y * Settings.scale;
		this.scale = Settings.scale;
	}

	public void setVisible(boolean b) {
		isVisible = b;
	}

	public void render(SpriteBatch sb) {
		if (isVisible) {
			sb.setColor(Color.WHITE);
			sb.draw(texture, hb.cX - 64.0F, hb.cY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, 0, 0,
					0, 128, 128, false, false);
			if (hb.hovered) {
				renderTip(sb);
			}

			sb.draw(ImageMaster.UI_GOLD, hb.cX + GOLD_OFFSET_X, hb.cY + GOLD_OFFSET_Y, GOLD_IMG_WIDTH, GOLD_IMG_WIDTH);

			Color priceColor = Color.WHITE;
			if (!canBuy()) {
				priceColor = Color.SALMON;
			}
			FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, Integer.toString(price),
					hb.cX + PRICE_OFFSET_X, hb.cY + PRICE_OFFSET_Y, priceColor);
		}

	}

	private void renderTip(SpriteBatch sb) {
		if (InputHelper.mX < 1400.0F * Settings.scale) {
			TipHelper.queuePowerTips(InputHelper.mX + 60.0F * Settings.scale, InputHelper.mY - 30.0F * Settings.scale,
					this.tips);
		} else {
			TipHelper.queuePowerTips(InputHelper.mX - 350.0F * Settings.scale, InputHelper.mY - 50.0F * Settings.scale,
					this.tips);
		}
	}

	public void update(float rugY) {
		if (isVisible) {
			hb.move(x, rugY + y);
			hb.update();

			if (hb.hovered) {
				this.shopScreen.moveHand(hb.cX - 190.0F * Settings.scale, hb.cY - 70.0F * Settings.scale);
				if (InputHelper.justClickedLeft) {
					hb.clickStarted = true;
				}
				scale = Settings.scale * 1.25f;
			} else {
				scale = Settings.scale;
			}

			if (hb.clicked) {
				hb.clicked = false;
				tryPurchase();
			}
		}
	}

	private void tryPurchase() {
		if (canBuy()) {
			AbstractDungeon.player.loseGold(price);
			CardCrawlGame.sound.play("SHOP_PURCHASE", 0.1F);
			this.shopScreen.playBuySfx();
			this.shopScreen.createSpeech(ShopScreen.getBuyMsg());
			if (!AbstractDungeon.player.hasRelic("The Courier")) {
				this.isVisible = false;
			}
			onPurchase();
		} else {
			this.shopScreen.playCantBuySfx();
			this.shopScreen.createSpeech(ShopScreen.getCantBuyMsg());
		}
	}

	public boolean canBuy() {
		return AbstractDungeon.player.gold >= price;
	}

	protected abstract void onPurchase();
}
