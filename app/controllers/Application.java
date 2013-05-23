package controllers;

import cn.bran.play.JapidController;

public class Application extends JapidController {

	public static void index() {
		render();
	}

	public static void home() {
		renderJapid();
	}

}