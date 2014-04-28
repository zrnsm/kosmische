(
	SynthDef("Kosmische", {|note = 45, 
		osc1_level = 10.0, 
		osc1_type = 0, 
		osc1_detune = 0, 
		osc1_width = 0.5, 
		osc1_octave = 0,
		osc1_tune = 0,

		osc2_level = 10.0, 
		osc2_type = 0, 
		osc2_detune = 0, 
		osc2_width = 0.5, 
		osc2_octave = 0,
		osc2_tune = 0,
		
		lfo1_type = 0,
		lfo1_freq = 0,
       lfo1_depth = 0,
       lfo1_target = 0,

       lfo2_type = 0,
		lfo2_freq = 0,
       lfo2_depth = 0,
       lfo2_target = 0,

       lfo3_type = 0,
		lfo3_freq = 0,
       lfo3_depth = 0,
       lfo3_target = 0,

       lfo4_type = 0,
		lfo4_freq = 0,
       lfo4_depth = 0,
       lfo4_target = 0,
		
		amp_attack = 0.001,
		amp_decay = 0.5,
		amp_sustain = 0.1,
		amp_release = 0.1,
		cutoff = 5000,
		resonance = 1,
		filter_attack = 0.001,
		filter_decay = 0.5,
		filter_sustain = 0.1,
		filter_release = 0.1,
		filter_env_amount = -1,
		delaytime = 0.5,
		decaytime = 1.0,
		reverb_mix = 0.5,
		delay_mix = 0,
		reverb_room_size = 0.5,
		reverb_damp = 0.5,
		trigger = 0|

	  	var osc1_freq_bus = 2;
	  	var osc1_width_bus = 3;
	  	var osc2_freq_bus = 4;
	  	var osc2_width_bus = 5;
	  	var delay_time_bus = 6;
	  	var decay_time_bus = 7;
	  	var cutoff_bus = 8;
	  	var resonance_bus = 9;
	  	var lfo1, lfo2, lfo3, lfo4;
	  	var osc1, osc2;
	  	var osc1_freq_mod, osc2_freq_mod;
	  	var amp_env, filter_env;
	    var filtered, delayed, reverbed;

		lfo1 = Select.ar(lfo1_type, [
           SinOsc.ar(lfo1_freq, 0, lfo1_depth),
           LFTri.ar(lfo1_freq, 0, lfo1_depth),
			LFPulse.ar(lfo1_freq, 0, 0.5, lfo1_depth),
			Latch.ar(WhiteNoise.ar, Impulse.ar(lfo1_freq)) * lfo1_depth 
		]);
		Out.ar(2 + lfo1_target, lfo1);

		lfo2 = Select.ar(lfo2_type, [
           SinOsc.ar(lfo2_freq, 0, lfo2_depth),
           LFTri.ar(lfo2_freq, 0, lfo2_depth),
			LFPulse.ar(lfo2_freq, 0, 0.5, lfo2_depth),
			Latch.ar(WhiteNoise.ar, Impulse.ar(lfo2_freq)) * lfo2_depth
		]);
		Out.ar(2 + lfo2_target, lfo2);

		lfo3 = Select.ar(lfo3_type, [
           SinOsc.ar(lfo3_freq, 0, lfo3_depth),
           LFTri.ar(lfo3_freq, 0, lfo3_depth),
			LFPulse.ar(lfo3_freq, 0, 0.5, lfo3_depth),
			Latch.ar(WhiteNoise.ar, Impulse.ar(lfo3_freq)) * lfo3_depth
		]);
		Out.ar(2 + lfo3_target, lfo3);

		lfo4 = Select.ar(lfo4_type, [
           SinOsc.ar(lfo4_freq, 0, lfo4_depth),
           LFTri.ar(lfo4_freq, 0, lfo4_depth),
			LFPulse.ar(lfo4_freq, 0, 0.5, lfo4_depth),
			Latch.ar(WhiteNoise.ar, Impulse.ar(lfo4_freq)) * lfo4_depth
		]);
		Out.ar(2 + lfo4_target, lfo4);

		osc1_freq_mod = In.ar(osc1_freq_bus);
		osc1 = osc1_level.log10 * Select.ar(osc1_type, [
			Saw.ar((note + osc1_tune + (osc1_octave * 12)).midicps + osc1_detune + osc1_freq_mod),
			Pulse.ar((note + osc1_tune + (osc1_octave * 12)).midicps + osc1_detune + osc1_freq_mod, osc1_width + In.ar(osc1_width_bus).clip(-0.5, 0.5)), 
			// unfortunately the repitition of the frequency calculation is necessary here to keep this contained in a single synthdef
			SinOsc.ar((note + osc1_tune + (osc1_octave * 12)).midicps + osc1_detune + osc1_freq_mod),
			WhiteNoise.ar
		]);

       osc2_freq_mod = In.ar(osc1_freq_bus);
		osc2 = osc2_level.log10 * Select.ar(osc2_type, [
			Saw.ar((note + osc2_tune + (osc2_octave * 12)).midicps + osc2_detune + osc2_freq_mod),
			Pulse.ar((note + osc2_tune + (osc2_octave * 12)).midicps + osc2_detune + osc2_freq_mod, osc2_width + In.ar(osc2_width_bus).clip(-0.5, 0.5)),
			SinOsc.ar((note + osc2_tune + (osc2_octave * 12)).midicps + osc2_detune + osc2_freq_mod),
			WhiteNoise.ar
		]);
		
		amp_env = EnvGen.kr(Env.adsr(amp_attack, amp_decay, amp_sustain, amp_release, 1, 0), trigger);
		filter_env = XFade2.kr(DC.kr(1), EnvGen.kr(Env.adsr(filter_attack, filter_decay, filter_sustain, filter_release, 1, 0), trigger), filter_env_amount);
		filtered = MoogFF.ar(amp_env * Mix.new([osc1, osc2]), (cutoff + In.ar(cutoff_bus)) * filter_env, resonance + In.ar(resonance_bus));
		delayed = XFade2.ar(filtered, CombC.ar(filtered, 2.0, delaytime + In.ar(delay_time_bus), decaytime + In.ar(decay_time_bus)), 0);
		reverbed = FreeVerb.ar(delayed, reverb_mix, reverb_room_size, reverb_damp);
		Out.ar(0, [reverbed, reverbed]);	
	}).writeDefFile("/Users/nicolas/git/kosmische/assets")
)

x = Synth("Kosmische");
x.set(\filter_env_amount, 1);
x.set(\note, 40);
x.set(\trigger, 1);
x.set(\cutoff, 10000);

x.set(\lfo1_freq, 0.3);
x.set(\lfo1_type, 0);
x.set(\lfo1_depth, 0.5);
x.set(\lfo1_target, 1);

x.set(\lfo2_freq, 0.4);
x.set(\lfo2_type, 0);
x.set(\lfo2_depth, 2);
x.set(\lfo2_target, 0);

x.set(\osc1_type, 1);
x.set(\osc2_level, 1);

x.set(\trigger, 0);
x.set(\osc1_detune, 0);
x.set(\osc1_octave, 2);
x.set(\osc1_level, 0.5);
x.set(\osc1_type, 2);
x.set(\type_1, 1);
x.set(\osc2_octave, 2);
x.set(\osc2_detune, 2);
x.set(\osc2_level, 0);
x.set(\osc2_type, 2);
x.free;