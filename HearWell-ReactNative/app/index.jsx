import React from "react";
import { StyleSheet, Button, View, Text } from "react-native";
import { Audio } from 'expo-av';

export default function index() {
    const [recording, setRecording] = React.useState();
    const [recordings, setRecordings] = React.useState([]);

    async function startRecording() {
        try {
            const perm = await Audio.requestPermissionsAsync();
            if(perm.status === "granted") {
                await Audio.setAudioModeAsync({
                    allowsRecordingIOS: true,
                    playsInSilentModeIOS: true
                });
                const { recording } = await Audio.Recording.createAsync(Audio.RECORDING_OPTIONS_PRESET_HIGH_QUALITY);
                setRecording(recording);
            }
        } catch(err) {}
    }

    async function stopRecording() {
        setRecording(undefined);

        await recording.stopAndUnloadAsync();
        let allRecordings = [...recordings];
        const { sound, status } = await recording.createNewLoadedSoundAsync();
        allRecordings.push({
            sound: sound,
            duration: getDurationFormatted(status.durationMillis),
            file: recording.getURI()
        })

        setRecordings(allRecordings);
    }
    
    function clearRecordings() {
        setRecordings([]);
    }

    function getDurationFormatted(millis) {
        const minutes = millis/1000/60;
        const seconds = Math.round((minutes - Math.floor(minutes))*60);
        return seconds < 10 ? `${Math.floor(minutes)}:0${seconds}` : `${Math.floor(minutes)}:${seconds}`
    }

    function getRecordingLines() {
        return recordings.map((recordingLine, index) => {
            return (
                <View key={index} style={styles.row}>
                    <Text styles={styles.fill}>
                        Recording #{index+1} | {recordingLine.duration}
                    </Text>
                    <Button onPress={() => recordingLine.sound.replayAsync()} title="Play"></Button>
                </View>
            );
        });
    }

    return (
        <View style={styles.container}>
            <Button title={recording ? "Stop Recording" : "Start Recording"} onPress={recording ? stopRecording : startRecording}></Button>
            {getRecordingLines()}
            <Button title={recordings.length > 0 ? "Clear Recordings" : ""} onPress={clearRecordings} style={recordings.length > 0 ? styles.exists : styles.nonexists}></Button>
        </View>
    );

    
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: "#fff",
        alignItems: "center",
        justifyContent: "center"
    },
    row: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        marginLeft: 10,
        marginRight: 40
    },
    fill: {
        flex: 1,
        margin: 15
    },
    nonexists: {
        backgroundColor: "#fff"
    },
    exists: {
        backgroundColor: "orange"
    }
})