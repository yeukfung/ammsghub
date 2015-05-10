package controllers

import play.api.mvc.Controller
import play.api.mvc.Action

object ProfileController extends Controller {

  def index = TODO
  
  def edit(id:String) = TODO
  
  def wechatCRUD = Action {
    Ok(views.html.wechatprofile.index())
  }
  
  
}