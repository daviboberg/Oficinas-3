#!/usr/bin/python
import signal
from Accelerometer.AccService import AccService
from Communication.BluetoothService import BluetoothService
from Spotify.SpotifyClient import SpotifyClient

# Temp import
from Light.LightFunctions.color_gen import gen_rainbow_gradient
from Light.LightFunctions.handler_creators import create_wave_handler_args
from Light.LightFunctions.handlers import standard_handler, wave_handler
from Light.LightFunctions.modifiers import wave_modifier, gen_sine_wave
from Light.controller import TimedLightShow
from Light.LightFunctions.handlers import standard_handler, wave_handler

class Tesseract():
	def __init__(self):
		self.spotify = SpotifyClient(self)

		# TODO: Create LED control thread
		self.bluetooth_service = BluetoothService(self)
		self.acc_service = AccService(self)

		self.lightConfig()

		self.is_spotify = False

	def run(self):
		self.bluetooth_service.start()
		self.acc_service.start()
		self.lights = self.light_show.start()

	def stop_services(self, s, f):
		self.bluetooth_service.stop_service()
		self.acc_service.stop_service()
		self.lights = self.light_show.stop()


	def lightConfig(self):
		n_leds = 20

		gradient = gen_rainbow_gradient(0, 360, 1, 100)
		wave = gen_sine_wave(0.5, 3, n_leds)
		wave_handler_args = create_wave_handler_args(gradient, wave, 3, n_leds)

		self.light_show = TimedLightShow(wave_handler, wave_handler_args, 0.05, -1)


if __name__ == '__main__':
	import signal
	tesseract = Tesseract()
	signal.signal(signal.SIGINT, tesseract.stop_services)
	signal.signal(signal.SIGTERM, tesseract.stop_services)
	tesseract.run()
