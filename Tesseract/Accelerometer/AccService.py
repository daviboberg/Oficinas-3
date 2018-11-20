import multiprocessing
from Accelerometer.Accelerometer import Accelerometer
from Accelerometer.AccReading import AccReading
from Spotify.SpotifyClient import SpotifyClient
from threading import Thread


class AccService(multiprocessing.Process):
	def __init__(self, tesseract, bluetooth_queue):
		super().__init__()
		self.tesseract = tesseract
		self.accelerometer = Accelerometer()
		self.bluetooth_queue = bluetooth_queue
		self.spotify_client = SpotifyClient()

		self.thread_communication_list = [self.spotify_client]
		self.queue_thread = Thread(target=self.read_queue)

		self._stop_service = False

	def read_queue(self):
		while True:
			msg = self.bluetooth_queue.get()

			print("spotify message received!")

			if msg["type"] == "spotify":
				spotify_client = self.thread_communication_list[0]

				if msg["subtype"] == "disconnect":
					spotify_client.is_active = False

				elif msg["subtype"] == "connect":
					spotify_client.connect(msg["value"]["token"], msg["value"]["deviceID"])

			else:
				print("invalid message received by spotify process")

	def stop_service(self):
		self._stop_service = True

	def run(self):
		self.queue_thread.start()

		while not self._stop_service:
			reading = self.accelerometer.wait_for_movement()
			if reading == AccReading.INC_RIGHT:
				self.inclined_right()
			elif reading == AccReading.INC_LEFT:
				self.inclined_left()
			elif reading == AccReading.INC_FRONT:
				self.inclined_front()
			elif reading == AccReading.INC_BACK:
				self.inclined_back()
			elif reading == AccReading.UP_DOWN:
				self.up_and_down()
			elif reading == AccReading.AGITATION:
				self.agitated()

	def inclined_right(self):
		if self.spotify_client.is_active:
			self.spotify_client.next_track()

	def inclined_left(self):
		if self.spotify_client.is_active:
			self.spotify_client.previous_track()

	def inclined_front(self):
		pass

	def inclined_back(self):
		pass

	def up_and_down(self):
		if self.spotify_client.is_active:
			if self.spotify_client.is_playing():
				self.spotify_client.pause()
			else:
				self.spotify_client.pause()

	def agitated(self):
		if self.spotify_client.is_active:
			self.tesseract.spotify_client.shuffle()
