package v1.post

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ Future, ExecutionContext }

import play.api.libs.json._

class PostExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait PostRepository {
  def create(data: PostEntity)(implicit mc: MarkerContext): Future[PostEntity]

  def list()(implicit mc: MarkerContext): Future[Iterable[PostEntity]]

  def get(id: Long)(implicit mc: MarkerContext): Future[Option[PostEntity]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class PostRepositoryImpl @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: PostExecutionContext) extends PostRepository {

  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

    /**
     * Here we define the table. It will have a name of people
     */
    private class PostTable(tag: Tag) extends Table[PostEntity](tag, "Post") {

      /** The ID column, which is the primary key, and auto incremented */
      def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

      /** The link column */
      def link = column[String]("link")

      /** The title column */
      def title = column[String]("title")

      /** The body column */
      def body = column[String]("body")

      /**
       * This is the tables default "projection".
       *
       * It defines how the columns are converted to and from the Person object.
       *
       * In this case, we are simply passing the id, name and page parameters to the Person case classes
       * apply and unapply methods.
       */
      def * = (id, link, title, body) <> ((PostEntity.apply _).tupled, PostEntity.unapply)
    }


  /**
   * The starting point for all queries on the people table.
   */
  private val post = TableQuery[PostTable]

  private val logger = Logger(this.getClass)

  /**
   * List all the posts in the database.
   */
  override def list()(implicit mc: MarkerContext): Future[Iterable[PostEntity]] = db.run {
    post.result
  }

  override def get(id: Long)(implicit mc: MarkerContext): Future[Option[PostEntity]] = db.run {
    post.filter(_.id === id).result.headOption
  }

  def create(data: PostEntity)(implicit mc: MarkerContext): Future[PostEntity] = db.run {

    val pair = (data.link, data.title, data.body)
    val res = (post.map(p => (p.link, p.title, p.body))
      returning post.map(_.id)
      into ((linkTitleBody, id) => PostEntity(id, linkTitleBody._1, linkTitleBody._2, linkTitleBody._3))
    )
    res += pair
  }

}
