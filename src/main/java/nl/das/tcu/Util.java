/*
 * Copyright © 2021 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 12 Aug 2021.
 */


package nl.das.tcu;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import nl.das.tcu.objects.Terrarium;

/**
 *
 */
public class Util {

	static DateTimeFormatter dtfmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	static DateTimeFormatter tffmt = DateTimeFormatter.ofPattern("yyyyMMdd");


	public static String cvtPeriodToString(long l) {
		if (l == -1) {
			return "endlessly";
		}
		if (l == -2) {
			return "until ideal temperature has been reached";
		}
		if (l > 0) {
			LocalTime t = Util.ofEpochSecond(l).toLocalTime();
			return String.format("until %02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond());
		}
		return "";
	}

	public static String cvtTimeToString(long l) {
    if (l == 0L) {
      return "00:00:00";
    }
		LocalTime t = Util.ofEpochSecond(l).toLocalTime();
		return String.format("%02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond());
	}

	public static long cvtStringToMinutes(String hhmm) {
        if (hhmm.contains(":")) {
		    return (Long.parseLong(hhmm.split(":")[0]) * 60L) + (Long.parseLong(hhmm.split(":")[1]));
        } else {
            return 0L;
        }
	}

	public static long now(LocalDateTime now) {
		return now.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
	}

	public static LocalDateTime ofEpochSecond(long epochseconds) {
		return LocalDateTime.ofEpochSecond(epochseconds, 0, ZoneId.systemDefault().getRules().getOffset(Instant.now()));
	}

	public static String getDateTimeString() {
		return Terrarium.getNow().format(dtfmt) + " ";
	}

	public static List<String> listTraceFiles(String folder, String prefix) throws IOException {
	    List<String> fileList = new ArrayList<>();
	    Files.walkFileTree(Paths.get(folder), new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	            if (!Files.isDirectory(file) && file.getFileName().toString().startsWith(prefix)) {
	                fileList.add(file.getFileName().toString());
	            }
	            return FileVisitResult.CONTINUE;
	        }
	    });
	    Collections.sort(fileList);
	    return fileList;
	}

	public static String createStateTraceFile(String dir, LocalDateTime now) {
		// Count the number of tracefiles.
		// If > Terrarium.maxNrOfTraceDays delete oldest first
		// If file exists, delete it first
		try {
			List<String> files = listTraceFiles(dir, "state_");
			if (files.size() == Terrarium.maxNrOfTraceDays) {
				Files.deleteIfExists(Paths.get(dir + "/" + files.get(0)));
			}
			Path p = Paths.get(dir + "/state_" + now.format(tffmt));
			Files.deleteIfExists(p);
			Files.createFile(p);
			TimeUnit.SECONDS.sleep(1);
			return p.getFileName().toString();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String createTemperatureTraceFile(String dir, LocalDateTime now) {
		// Count the number of tracefiles.
		// If > Terrarium.maxNrOfTraceDays delete oldest first
		// If file exists, delete it first
		try {
			List<String> files = listTraceFiles(dir, "temp_");
			if (files.size() == Terrarium.maxNrOfTraceDays) {
				Files.deleteIfExists(Paths.get(dir + "/" + files.get(0)));
			}
			Path p = Paths.get(dir + "/temp_" + now.format(tffmt));
			Files.deleteIfExists(p);
			Files.createFile(p);
			TimeUnit.SECONDS.sleep(1);
			return p.getFileName().toString();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void traceState(String tracefile, LocalDateTime now, String fmt, Object ...args) {
		try {
			if (Terrarium.getInstance().isTraceOn()) {
				String nowstr = now.format(dtfmt);
				String format = nowstr + " " + fmt;
				Files.writeString(
						Paths.get(tracefile),
						String.format(format + "\n", args),
						StandardOpenOption.APPEND
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void traceTemperature(String tracefile, LocalDateTime now, String fmt, Object ...args) {
		try {
			if (Terrarium.getInstance().isTraceOn()) {
				String nowstr = now.format(dtfmt);
				String format = nowstr + " " + fmt;
				Files.writeString(
						Paths.get(tracefile),
						String.format(format + "\n", args),
						StandardOpenOption.APPEND
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Jsonb parser() {
		Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(true));
		return jsonb;
	}

	public static void println(String message) {
        if (TCU.DEBUG_ON) {
		    System.out.println(getDateTimeString() + "[" + Thread.currentThread().getName() + "] " + message);
        }
	}
}
