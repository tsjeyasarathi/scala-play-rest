package v1.post

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * Controls access to the backend data, returning [[PostEntity]]
  */
class PostResourceHandler @Inject()(
    routerProvider: Provider[PostRouter],
    postRepository: PostRepository)(implicit ec: ExecutionContext) {

  def create(postInput: PostEntity)(implicit mc: MarkerContext): Future[PostEntity] = {
    val data = PostEntity(999, postInput.link, postInput.title, postInput.body)
    postRepository.create(data).map { d =>
      PostEntity(d.id, d.link, d.title, d.body)
    }
  }

  def lookup(id: Long)(implicit mc: MarkerContext): Future[Option[PostEntity]] = {
    val postFuture = postRepository.get(id)
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        postData
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[PostEntity]] = {
    postRepository.list().map { postDataList =>
      postDataList.map(postData => postData)
    }
  }

}
