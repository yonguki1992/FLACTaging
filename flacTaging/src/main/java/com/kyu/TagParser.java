package com.kyu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.kyu.vo.TagVO;


public class TagParser {
	private static final int ALBUM = 0;
	private static final int YEAR = 1;
	private static final int GENRE = 2;
	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		
		System.out.print("please input filePath > ");
		String filePath1 = sc.nextLine();
		System.out.print("pleas input sourceUrl > ");
		//ex: "https://www.melon.com/song/detail.htm?songId=9604149&";
		String sourceUrl = sc.nextLine();
		

		
		try {
			
			go(new File(filePath1), sourceUrl);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("그런 디렉터리는 없습니다");
		} finally {
			System.out.println("태그 불러오기 종료");
		}

	}



	// jaudio 이용해서 음원 태그 정보 얻어와서 태그 수정하기
	private static void go(File fs, String sourceUrl) throws FileNotFoundException {
		System.out.println("go 호출 fs="+fs);
		if (fs.isDirectory()) {
			File list[] = fs.listFiles();
			TagVO vo = new TagVO();

			int fileCount = 0;
			for (File f : list) {
				try {
					/*
					 * 이건 mp3 파일 일때 MP3File mp3 = (MP3File) AudioFileIO.read(f); AbstractID3v2Tag
					 * tag2 = mp3.getID3v2Tag(); Tag tag = mp3.getTag();
					 * 
					 * String title = tag.getFirst(FieldKey.TITLE); String artist =
					 * tag.getFirst(FieldKey.ARTIST); String album = tag.getFirst(FieldKey.ALBUM);
					 * String year = tag.getFirst(FieldKey.YEAR); String genre =
					 * tag.getFirst(FieldKey.GENRE);
					 */

					// flac 파일
					AudioFile flacFile = AudioFileIO.read(f);
					FlacTag tag = (FlacTag) flacFile.getTag();

					System.out.println("Tag : " + tag);
					System.out.println("title : " + tag.getFirst(FieldKey.TITLE));
					System.out.println("Artist : " + tag.getFirst(FieldKey.ARTIST));
					System.out.println("Album : " + tag.getFirst(FieldKey.ALBUM));
					System.out.println("Year : " + tag.getFirst(FieldKey.YEAR));
					System.out.println("Genre : " + tag.getFirst(FieldKey.GENRE));
					
					HashMap<String, String> cssQueryMap = makeQuery();
					
					String url = "";
					
					int offset = 0;
					
					if(sourceUrl.contains("&")) {
						String tempUrl = sourceUrl.substring(0, sourceUrl.indexOf("&"));
						url = tempUrl.substring(0, tempUrl.length() - 2);
						offset = Integer.parseInt(tempUrl.substring(tempUrl.length() - 2, tempUrl.length()));
					} else {
						url = sourceUrl.substring(0, sourceUrl.length() - 2);
						offset = Integer.parseInt(sourceUrl.substring(sourceUrl.length() - 2, sourceUrl.length()));
					}
							

					vo = getTag(String.format(url + "%02d", offset + fileCount ), cssQueryMap);
					
					if(vo != null) {
						setTag(tag, vo);
						flacFile.commit();
						
						StringBuilder sb = new StringBuilder();
						sb.append(fs.getPath())
							.append("\\")
							.append(String.format("%02d ", fileCount+1))
							.append(vo.getTitle())
							.append(".flac");
					    File fileNew = new File(sb.toString());
					    if( f.exists() ) f.renameTo(fileNew);
					}
					fileCount++;
					
				} catch (Exception ex) {
					System.out.println("얻어오기 실패");
				}
			}
		} else {
			System.out.println("디렉토리가 아닙니다.");
		}
	}

	private static void setTag(FlacTag tag, TagVO vo) throws FieldDataInvalidException {
		System.out.println("setTag 호출 vo="+vo);
		tag.setField(FieldKey.TITLE, vo.getTitle());
		tag.setField(FieldKey.ARTIST, vo.getArtist());
		tag.setField(FieldKey.ALBUM, vo.getListInfo().get(ALBUM));
		tag.setField(FieldKey.YEAR, vo.getListInfo().get(YEAR));
		tag.setField(FieldKey.GENRE, vo.getListInfo().get(GENRE));
		System.out.println("setTag 리턴");
	}

	// jsoup 이용해서 태그 받아오기
	private static TagVO getTag(String url, Map<String, String> cssQueryMap) {
		System.out.println("getTag 호출 url="+url+", cssQueryMap="+cssQueryMap);

		TagVO vo = null;
		Document doc = null;
		
		try {

			vo = new TagVO();
			doc = Jsoup.connect(url).get();

			Elements titleElements = doc.select(cssQueryMap.get("title"));
			Elements artistElements = doc.select(cssQueryMap.get("artist"));
			Elements listElements = doc.select(cssQueryMap.get("list"));


			System.out.println("페이지 제목 : " + doc.title());
			System.out.println("title 사이즈 : " + titleElements.size());
			System.out.println("artist 사이즈 : " + artistElements.size());
			System.out.println("list 사이즈 : " + listElements.size());

			if (!titleElements.isEmpty() && !artistElements.isEmpty() &&!listElements.isEmpty()) {
				String title = ""; // 태그 - 제목
				String artist = ""; // 태그 - 작자
				List<String> listInfo = new ArrayList<String>(); // 태그 - 나머지들
				for (Element element : titleElements) {
					title = element.ownText();
					System.out.println(title);
					vo.setTitle(title);
				}

				for (Element element : artistElements) {
					artist = element.text();
					System.out.println(artist);
					vo.setArtist(artist);
				}

				
				for (Element element : listElements) {
					listInfo.add(element.text());
				}
				vo.setListInfo(listInfo);
				System.out.println(listInfo);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("getTag 리턴 vo="+vo);

		return vo;
	}
	
	private static HashMap<String, String> makeQuery() {
		String queryForTitle = "div.song_name:not(strong.none)";
		String queryForArtist = "div.artist span:not(.thumb_atist)";
		String queryForList = "div.meta > dl.list > dd";

		HashMap<String, String> cssQueryMap = new HashMap<String, String>();
		cssQueryMap.put("title", queryForTitle);
		cssQueryMap.put("artist", queryForArtist);
		cssQueryMap.put("list", queryForList);
		return cssQueryMap;
	}
}

