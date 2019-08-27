package playlistoptimizer.domain

case class Playlist(songs: IndexedSeq[Song]) {
  def size: Int = songs.size

  def map(f: IndexedSeq[Song] => IndexedSeq[Song]): Playlist = Playlist(f(songs))

  def zipWith(otherPlaylist: Playlist)(f: (IndexedSeq[Song], IndexedSeq[Song]) => IndexedSeq[Song]): Playlist =
    Playlist(f(songs, otherPlaylist.songs))

  def reduce[T](f: IndexedSeq[Song] => T): T = f(songs)
}

object Playlist {
  def apply(songs: Song*): Playlist = new Playlist(Vector(songs: _*))
}