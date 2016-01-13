package com.dbfs.jtt.util;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.dbfs.jtt.Activator;

public class SoundUtil extends Thread implements LineListener {
	static boolean playCompleted;
	private final String filePath;

	public SoundUtil(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void run() {
		play(filePath);
	}

	private void play(String filePath) {
		AudioInputStream inpunStream = null;
		try {
			Bundle bundle = Activator.getDefault().getBundle();
			Path path = new Path(filePath);
			URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
			URL fileUrl = FileLocator.toFileURL(url);
			File file = new File(fileUrl.getPath());
			LogManager.log(Level.ALL, "SoundUtil", "Play Sound: " + fileUrl.getPath());
			inpunStream = AudioSystem.getAudioInputStream(file);
			DataLine.Info info = new DataLine.Info(Clip.class, inpunStream.getFormat());
			Clip audioClip = (Clip) AudioSystem.getLine(info);
			audioClip.open(inpunStream);
			audioClip.start();
			while (!playCompleted) {
				// wait for the playback completes
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					LogManager.logStack(ex);
				}
			}
			inpunStream.close();
			audioClip.close();

		} catch (Exception e) {
			LogManager.logStack(e);
		}

	}

	@Override
	public void update(LineEvent event) {
		LineEvent.Type type = event.getType();
		if (type == LineEvent.Type.START) {
			// System.out.println("Playback started.");
		} else if (type == LineEvent.Type.STOP) {
			playCompleted = true;
			// System.out.println("Playback completed.");
		}
	}

}
