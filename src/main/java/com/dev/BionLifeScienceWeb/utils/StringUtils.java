package com.dev.BionLifeScienceWeb.utils;

import java.util.Random;

public class StringUtils {
	
	public String createWord() {
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = 10;
	    Random random = new Random();
	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	                                   .limit(targetStringLength)
	                                   .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	                                   .toString();
		return generatedString;
	}

	public String changeDate(String date) {
		
		String newDate = "";
		String year = date.split(" ")[3]; // 연
		String month = date.split(" ")[1]; // 월
		String day = date.split(" ")[2]; // 일
		String today = date.split(" ")[0]; // 요일
		
		switch(month) {
			case "Jan" : month = "1";
			break;
			
			case "Feb" : month = "2";
			break;
			
			case "Mar" : month = "3";
			break;
			
			case "Apr" : month = "4";
			break;
			
			case "May" : month = "5";
			break;
			
			case "Jun" : month = "6";
			break;
			
			case "Jul" : month = "7";
			break;
			
			case "Aug" : month = "8";
			break;
			
			case "Sep" : month = "9";
			break;
			
			case "Oct" : month = "10";
			break;
			
			case "Nov" : month = "11";
			break;
			
			case "Dec" : month = "12";
			break;
		}
		
		switch(today){
			case "Mon" : today = "월";
			break;
			
			case "Tue" : today = "화";
			break;
			
			case "Wed" : today = "수";
			break;
			
			case "Thu" : today = "목";
			break;
			
			case "Fri" : today = "금";
			break;
			
			case "Sat" : today = "토";
			break;
			
			case "Sun" : today = "일";
			break;
		}
		newDate = year + "년 " + month + "월 " + day + "일(" + today + "요일)"; 
		return newDate;
	}
}
