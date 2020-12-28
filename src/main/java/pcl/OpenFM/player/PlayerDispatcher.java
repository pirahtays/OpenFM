package pcl.OpenFM.player;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.message.MessageRadioPlaying;
import net.minecraftforge.fml.client.FMLClientHandler;

public class PlayerDispatcher extends PlaybackListener implements Runnable {
	private String streamURL;
	public MP3Player mp3Player;
	public OGGPlayer oggPlayer;
	public AACPlayer aacPlayer;
	public String decoder;
	private Thread pThread;
	private int x;
	private int y;
	private int z;
	private World world;
	public PlayerDispatcher(String decoder, String mp3url, World w, int a, int b, int c)
	{
		try
		{
			this.world = w;
			this.decoder = decoder;
			this.x = a;
			this.y = b;
			this.z = c;
			this.streamURL = mp3url;
			this.pThread = new Thread(this);
			this.pThread.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		System.out.println(decoder);
		try
		{
			if (decoder.equals("mp3")) {
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder().url(streamURL).build();
				
				Response response = client.newCall(request).execute();
				InputStream stream = response.body().byteStream();
				 
				this.mp3Player = new MP3Player(stream);
				this.mp3Player.setID(this.world, this.x, this.y, this.z);
				this.mp3Player.setPlayBackListener(this);
				this.mp3Player.play();
			} else if (decoder.equals("ogg")) {
				this.oggPlayer = new OGGPlayer();
				this.oggPlayer.setID(this.world, this.x, this.y, this.z);
				this.oggPlayer.play(this.streamURL);
			} else if (decoder.equals("aac")) {
				//this.aacPlayer = new AACPlayer();
				//this.aacPlayer.setID(this.world, this.x, this.y, this.z);
				//this.aacPlayer.play(this.streamURL);
			}
		}
		catch (Exception e)
		{
			PacketHandler.INSTANCE.sendToServer(new MessageRadioPlaying(this.x, this.y, this.z, false).wrap());
			FMLClientHandler.instance().getClient().player.sendMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.invalid_link")));
			OpenFM.logger.error(e);
		}
	}

	public void stop()
	{
		if ((this.mp3Player != null || this.oggPlayer != null || this.aacPlayer != null) && (isPlaying()))
		{
			if (decoder.equals("mp3")) {
				this.mp3Player.stop();
			} else if (decoder.equals("ogg")) {
				this.oggPlayer.stop();
			} else if (decoder.equals("aac")) {
				this.aacPlayer.stop();
			}
		}
	}

	@Override
	public void playbackStarted(PlayBackEvent evt) {}

	@Override
	public void playbackFinished(PlayBackEvent evt) {}

	public boolean isPlaying()
	{
		if (decoder.equals("mp3")) {
			return this.pThread.isAlive();
		} else if (decoder.equals("ogg")) {
			return this.oggPlayer.isPlaying();
		} else if (decoder.equals("aac")) {
			return this.aacPlayer.isPlaying();
		} else {
			return false;
		}

	}

	public void setVolume(float f)
	{
		if (this.mp3Player != null) {
			this.mp3Player.setVolume(f);
		} else if (this.oggPlayer != null) {
			this.oggPlayer.setVolume(f);
		} else if (this.aacPlayer != null) {
			this.aacPlayer.setVolume(f);
		}
	}

	public float getVolume()
	{
		if (decoder.equals("mp3")) {
			return this.mp3Player.getVolume() / Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
		} else if (decoder.equals("ogg")) {
			return this.oggPlayer.getVolume() / Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
		} else if (decoder.equals("aac")) {
			return this.aacPlayer.getVolume() / Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
		}else {
			return 0;
		}
	}
}