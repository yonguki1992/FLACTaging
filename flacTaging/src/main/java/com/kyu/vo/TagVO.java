package com.kyu.vo;

import java.util.List;

public class TagVO {
	private String			title;
	private String			artist;
	private List<String>	listInfo;	//album, year, genre
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public List<String> getListInfo() {
		return listInfo;
	}
	public void setListInfo(List<String> listInfo) {
		this.listInfo = listInfo;
	}
	@Override
	public String toString() {
		return "TagVO [title=" + title + ", artist=" + artist + ", listInfo=" + listInfo + "]";
	}
	
	

	
}
