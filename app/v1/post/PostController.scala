package v1.post

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Takes HTTP requests and produces JSON.
  */
class PostController @Inject()(cc: PostControllerComponents)(implicit ec: ExecutionContext)
    extends PostBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[PostEntity] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "id" -> longNumber,
        "link" -> nonEmptyText,
        "title" -> nonEmptyText,
        "body" -> text
      )(PostEntity.apply)(PostEntity.unapply)
    )
  }

  def index: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("index: ")
    postResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: Long): Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace(s"show: id = $id")
    postResourceHandler.lookup(id).map { post =>
      Ok(Json.toJson(post))
    }
  }

  private def processJsonPost[A]()(implicit request: PostRequest[A]): Future[Result] = {
    def failure(badForm: Form[PostEntity]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PostEntity) = {
      postResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
