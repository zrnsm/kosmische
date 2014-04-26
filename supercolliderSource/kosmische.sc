{\rtf1\ansi\ansicpg1252\cocoartf1265\cocoasubrtf190
\cocoascreenfonts1{\fonttbl\f0\fnil\fcharset0 SourceCodePro-Regular;}
{\colortbl;\red255\green255\blue255;\red0\green0\blue191;\red96\green96\blue96;\red0\green115\blue0;
}
\pard\tx560\tx1120\tx1680\tx2240\tx2800\tx3360\tx3920\tx4480\tx5040\tx5600\tx6160\tx6720\pardirnatural

\f0\fs28 \cf0 (\
	var n\
)\
\
(\
	\cf2 SynthDef\cf0 (\cf3 "Kosmische"\cf0 , \{\cf2 |note = 45, \
		osc1_level = 0.5, \
		osc1_type = 0, \
		osc1_detune = 0, \
		osc1_width = 0.5, \
		osc1_octave = 0,\
		osc1_tune = 0,\
		osc2_level = 0.5, \
		osc2_type = 0, \
		osc2_detune = 0, \
		osc2_width = 0.5, \
		osc2_octave = 0,\
		osc2_tune = 0,\
		amp_attack = 0.001,\
		amp_decay = 0.5,\
		amp_sustain = 0.1,\
		amp_release = 0.1,\
		cutoff = 5000,\
		resonance = 1,\
		filter_attack = 0.001,\
		filter_decay = 0.5,\
		filter_sustain = 0.1,\
		filter_release = 0.1,\
		filter_env_amount = -1,\
		delaytime = 0.5,\
		decaytime = 1.0,\
		reverb_mix = 0.5,\
		reverb_room_size = 0.5,\
		reverb_damp = 0.5|\
		\cf0 \
		\cf2 var\cf0  osc1 = osc1_level * \cf2 Select\cf0 .ar(osc1_type, [\
			\cf2 Saw\cf0 .ar((note + osc1_tune + (osc1_octave * 12)).midicps + osc1_detune),\
			\cf2 Pulse\cf0 .ar((note + osc1_tune + (osc1_octave * 12)).midicps + osc1_detune, osc1_width), \
			// unfortunately the repitition of the frequency calculation is necessary here to keep this contained in a single synthdef\
			\cf2 SinOsc\cf0 .ar((note + osc1_tune + (osc1_octave * 12)).midicps + osc1_detune),\
			\cf2 WhiteNoise\cf0 .ar\
		]);\
		\cf2 var\cf0  osc2 = osc2_level * \cf2 Select\cf0 .ar(osc2_type, [\
			\cf2 Saw\cf0 .ar((note + osc2_tune + (osc2_octave * 12)).midicps + osc2_detune),\
			\cf2 Pulse\cf0 .ar((note + osc2_tune + (osc2_octave * 12)).midicps + osc2_detune, osc2_width),\
			\cf2 SinOsc\cf0 .ar((note + osc2_tune + (osc2_octave * 12)).midicps + osc2_detune),\
			\cf2 WhiteNoise\cf0 .ar\
		]);\
		\cf2 var\cf0  amp_env = \cf2 EnvGen\cf0 .kr(\cf2 Env\cf0 .adsr(amp_attack, amp_decay, amp_sustain, amp_release, 1, 0), \cf2 MouseButton\cf0 .kr(0, 1));\
		\cf2 var\cf0  filter_env = XFade2.kr(DC.kr(1), \cf2 EnvGen\cf0 .kr(\cf2 Env\cf0 .adsr(filter_attack, filter_decay, filter_sustain, filter_release, 1, 0), \cf2 MouseButton\cf0 .kr(0, 1)), filter_env_amount);\
		\cf2 var\cf0  filtered = \cf2 MoogFF\cf0 .ar(amp_env * \cf2 Mix\cf0 .new([osc1, osc2]), \cf2 cutoff * filter_env\cf0 , \cf2 resonance\cf0 );\
		\cf2 var\cf0  with_delay = \cf2 XFade2\cf0 .ar(filtered, \cf2 CombC\cf0 .ar(filtered, 2.0, delaytime, decaytime), 0);\
		var reverb = \cf2 FreeVerb\cf0 .ar(with_delay, reverb_mix, reverb_room_size, reverb_damp);\
		\cf2 Out\cf0 .ar(0, [\cf2 reverb\cf0 , \cf2 reverb\cf0 ]);	\
	\}).add\
)\
\
\cf2 Class\cf0 .browse\
\
x = \cf2 Synth\cf0 (\cf3 "Kosmische"\cf0 );\
x.set(\\filter_env_amount, 1);\
x.set(\cf4 \\note\cf0 , 70);\
x.set(\cf4 \\osc1_detune\cf0 , 0);\
x.set(\cf4 \\osc1_octave\cf0 , 2);\
x.set(\cf4 \\osc1_level\cf0 , 0.5);\
x.set(\\osc1_type, 2);\
x.set(\cf4 \\type_1\cf0 , 1);\
x.set(\cf4 \\osc2_octave\cf0 , 2);\
x.set(\\osc2_detune, 2);\
x.set(\cf4 \\osc2_level\cf0 , 0);\
x.set(\\osc2_type, 2);\
x.free;}