package bootstrap.liftweb

import _root_.net.liftweb._
import http.ResourceServer._
import actor._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._
import java.util.Locale
import java.sql.DriverManager
import _root_.net.liftweb.util.{ Props }
import _root_.net.liftweb.http.provider.HTTPRequest
import _root_.net.liftweb.http.auth.{ HttpBasicAuthentication, AuthRole, userRoles }

import code.model._
import code.snippet._

import net.liftmodules.{FoBo,FoBoFontAwesome}

object localeOverride extends SessionVar[Box[Locale]](Empty)

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {

   //If using defaults FoBo init params can be omitted
    FoBo.InitParam.JQuery=FoBo.JQuery182  
    FoBo.InitParam.ToolKit=FoBo.Bootstrap222
    FoBo.InitParam.ToolKit=FoBo.Foundation215
    FoBo.InitParam.ToolKit=FoBo.PrettifyJun2011
    FoBo.InitParam.ToolKit=FoBo.JQueryMobile110
    FoBo.InitParam.ToolKit=FoBo.DataTables190
    FoBo.InitParam.ToolKit=FoBo.Knockout210
    FoBo.InitParam.ToolKit=FoBo.FontAwesome200
    FoBo.init()  
    
    
    

    // where to search snippet
    LiftRules.addToPackages("code")

    /*un-comment and switch to db of your liking */
    MySchemaHelper.initSquerylRecordWithInMemoryDB
    //MySchemaHelper.initSquerylRecordWithMySqlDB
    //MySchemaHelper.initSquerylRecordWithPostgresDB

    Props.mode match {
      case Props.RunModes.Development => {
        logger.info("RunMode is DEVELOPMENT")
        /*OBS! do no use this in a production env*/
        if (Props.getBool("db.schemify", false)) {
          MySchemaHelper.dropAndCreateSchema
        }
        // pass paths that start with 'console' to be processed by the H2Console servlet
        if (MySchemaHelper.isUsingH2Driver) {
          /* make db console browser-accessible in dev mode at /console 
           * see http://www.h2database.com/html/tutorial.html#tutorial_starting_h2_console 
           * Embedded Mode JDBC URL: jdbc:h2:mem:test User Name:test Password:test */
          logger.info("Set up H2 db console at /console ")
          LiftRules.liftRequest.append({
            case r if (r.path.partPath match { case "console" :: _ => true case _ => false }) => false
          })
        }
      }
      case Props.RunModes.Production => {
        logger.info("RunMode is PRODUCTION")
        if (Props.getBool("db.schemify", false)) {
          logger.warn("DB.SCHEMIFY is TRUE in production.props, db data will be reset on restart of app")
          MySchemaHelper.dropAndCreateSchema
        }else{
            logger.info("db.shemify is disabled in production.props")
        }        
      }
      case _                         => logger.info("RunMode is TEST, PILOT or STAGING")
    }    
          
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => 
        NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    //LiftRules.setSiteMap(SiteMap(entries: _*))
    LiftRules.setSiteMap(Paths.sitemap)
    
    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    //notice fade out (start after x, fade out duration y)
    LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) => {
      notices match {
        case NoticeType.Notice => Full((8 seconds, 4 seconds))
        case _                 => Empty
      }
    })

  }
}

object Paths {
  //import xml.NodeSeq
  import scala.xml._

  val divider1         = Menu("divider1") / "divider1"
  val divider2         = Menu("divider2") / "divider2"
  val divider3         = Menu("divider3") / "divider3"
  val hdivider1        = Menu("hdivider1") / "hdvidider1"
  //nav headers
  val navHeader1       = Menu.i("NavHeader1") / "navHeader1"
  val navHeader2       = Menu.i("NavHeader2") / "navHeader2"
  
  val content1DD       = Menu.i("Content1DD") / "ddlabel1"
  val content11DD      = Menu.i("Content11DD") / "ddlabel11"  
  
  val index            = Menu.i("Home") / "index"
  val liboIndex        = Menu.i("LiBo") / "libo"
  val libospyhome      = Menu(Loc("LiboSpyHome"  , Link(List("libospyhome")  , true, "#spyhome")  , S.loc("LiboSpyHome" , Text("Home"))      , LocGroup("liboSpyTop")))
  val libospyabout     = Menu(Loc("LiboSpyAbout" , Link(List("libospyabout") , true, "#spyabout") , S.loc("LiboSpyAbout", Text("About"))     , LocGroup("liboSpyTop")))
  val libospysetup     = Menu(Loc("LiboSpySetup" , Link(List("libospysetup") , true, "#spysetup") , S.loc("LiboSpySetup", Text("Setup"))     , LocGroup("liboSpyTop")))
  val libospyfooter    = Menu(Loc("LiboSpyFooter", Link(List("libospyfooter"), true, "#spyfooter"), S.loc("LiboSpyRef"  , Text("Referenser")), LocGroup("liboSpyTop")))
    
  val foundationDoc    = Menu(Loc("Foundation"     , Link(List("foundation")     , true, "/foundation/index")                     , "Foundation"))
  val bootstrap204Doc  = Menu(Loc("Bootstrap"      , Link(List("bootstrap")      , true, "/bootstrap/index")                      , "Bootstrap"))
  val bootstrap210Doc  = Menu(Loc("Bootstrap-2.1.0", Link(List("bootstrap-2.1.0"), true, "/bootstrap-2.1.0/index")                , S.loc("Bootstrap-2.1.0", Text("Bootstrap-2.1.0"))   ))
  val bootstrap220Doc  = Menu(Loc("Bootstrap-2.2.0", Link(List("bootstrap-2.2.0"), true, "/bootstrap-2.2.0/index")                , S.loc("Bootstrap-2.2.0", Text("Bootstrap-2.2.0")), LocGroup("nldemo1")  ))
  val bootstrap222Doc  = Menu(Loc("Bootstrap-2.2.2", Link(List("bootstrap-2.2.2"), true, "/bootstrap-2.2.2/index")                , S.loc("Bootstrap-2.2.2", Text("Bootstrap-2.2.2")), LocGroup("nldemo1")   ))
  val jqueryMobileDoc  = Menu(Loc("JQuery-mobile"  , Link(List("jquery-mobile")  , true, "/jquery-mobile/1.1.0/demos/index")      , "JQuery-mobile"))
  val datatablesDoc    = Menu(Loc("DataTables"     , Link(List("datatables")     , true, "/datatables/1.9.0/index")               , "DataTables"))
  val foboApiDoc       = Menu(Loc("FoBoAPI"        , Link(List("foboapi")        , true, "/foboapi/#net.liftmodules.FoBo.package"), S.loc("FoBoAPI"  , Text("FoBo API")), LocGroup("liboTop2","mdemo2","nldemo1") ))
  
  val nlHelp           = Menu.i("NLHelp") / "helpindex"

  
  def sitemap = SiteMap(
      navHeader1 >> LocGroup("nldemo1") >> FoBo.TBLocInfo.NavHeader,
      index >> LocGroup("mdemo1","nldemo1"),
      navHeader2 >> LocGroup("nldemo1") >> FoBo.TBLocInfo.NavHeader,
      liboIndex,
      hdivider1 >> LocGroup("mdemo1") >> FoBo.TBLocInfo.DividerVertical,
      libospyhome ,
      libospyabout,
      libospysetup,
      libospyfooter,
      content1DD >> LocGroup("liboTop1","mdemo1") >> PlaceHolder submenus ( 
          bootstrap210Doc ,
          bootstrap220Doc ,
          bootstrap222Doc ,
          divider1  >> FoBo.TBLocInfo.Divider,
          foboApiDoc 
      ),
      foundationDoc,
      jqueryMobileDoc,
      datatablesDoc,
      divider3 >> LocGroup("nldemo1") >> FoBo.TBLocInfo.Divider,
      nlHelp >> LocGroup("nldemo1")
      )
}
