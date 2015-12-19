package com.example.hypervision;

public class Line {
	public int getStartIndex() {
		return start;
	}

	public int getEndIndex() {
		return end;
	}

	private int start;
	private int end;

	public Line(int startIndex, int endIndex) {
		this.start = startIndex;
		this.end = endIndex;
	}
}
