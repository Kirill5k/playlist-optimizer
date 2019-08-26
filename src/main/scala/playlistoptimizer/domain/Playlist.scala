package playlistoptimizer.domain

case class Playlist(songs: Vector[Song]) {
  def size: Int = songs.size

  def map(f: Vector[Song] => Vector[Song]): Playlist = Playlist(f(songs))

  def zipWith(otherPlaylist: Playlist)(f: (Vector[Song], Vector[Song]) => Vector[Song]): Playlist =
    Playlist(f(songs, otherPlaylist.songs))

  def reduce[T](f: Vector[Song] => T): T = f(songs)
}