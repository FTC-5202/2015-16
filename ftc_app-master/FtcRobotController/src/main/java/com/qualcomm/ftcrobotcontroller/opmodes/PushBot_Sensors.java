package com.qualcomm.ftcrobotcontroller.opmodes;

/**
 * Created by Samuel on 9/24/2015.
 */
import com.qualcomm.robotcore.hardware.GyroSensor;
import java.util.Timer;
import java.util.TimerTask;

public class PushBot_Sensors extends PushBotTelemetry{

    public PushBot_Sensors() {

    }

    GyroSensor yeero;
    Timer t1 = new Timer("t1");
    double gyro_degrees = 0.0;
    double set_point = 0.0;
    final int GYRO_UPDATE_INTERVAL = 5;

    @Override public void start ()

    {
        run_using_encoders();
        yeero = hardwareMap.gyroSensor.get("gyro_sensor");
        set_point = yeero.getRotation();
    }



    @Override public void loop ()
    {

        //
        // Manage the drive wheel motors.
        //
        float l_left_drive_power = (float)scale_motor_power(-gamepad1.left_stick_y);
        float l_right_drive_power = (float)scale_motor_power(-gamepad1.right_stick_y);

        set_drive_power (l_left_drive_power, l_right_drive_power);

        //
        // Manage the arm motor.
        //
        float l_gp2_left_stick_y = -gamepad2.left_stick_y;
        float l_left_arm_power = (float)scale_motor_power (l_gp2_left_stick_y);
        v_motor_left_arm.setPower (l_left_arm_power);

        //----------------------------------------------------------------------
        //
        // Servo Motors
        //
        // Obtain the current values of the gamepad 'x' and 'b' buttons.
        //
        // Note that x and b buttons have boolean values of true and false.
        //
        // The clip method guarantees the value never exceeds the allowable range of
        // [0,1].
        //
        // The setPosition methods write the motor power values to the Servo
        // class, but the positions aren't applied until this method ends.
        //
        if (gamepad2.x)
        {
            m_hand_position (a_hand_position () + 0.05);
        }
        else if (gamepad2.b)
        {
            m_hand_position (a_hand_position () - 0.05);
        }
        if (gamepad1.right_bumper) {
            set_point = yeero.getRotation();
            gyro_degrees = 0.0;
        }

        t1.scheduleAtFixedRate(new UpdateDegrees(), GYRO_UPDATE_INTERVAL, GYRO_UPDATE_INTERVAL);

        //
        // Send telemetry data to the driver station.
        //
        update_telemetry(); // Update common telemetry
        telemetry.addData("10", "GP1 Left: " + -gamepad1.left_stick_y);
        telemetry.addData ("11", "GP1 Right: " + -gamepad1.right_stick_y);
        telemetry.addData ("12", "GP2 Left: " + l_gp2_left_stick_y);
        telemetry.addData ("13", "GP2 X: " + gamepad2.x);
        telemetry.addData ("14", "GP2 Y: " + gamepad2.b);
        telemetry.addData("Gyro", "Degrees = " + gyro_degrees);
        telemetry.addData("LE", "LEncoder = " + a_left_encoder_count());
        telemetry.addData("RE", "REncoder = " + a_right_encoder_count());
    }

    @Override
    public void stop(){
        t1.cancel();
    }

    class UpdateDegrees extends TimerTask {
        public void run(){
            final double BUFFER = 2.5;
            if(Math.abs(yeero.getRotation() - set_point) > BUFFER){
                gyro_degrees += (yeero.getRotation() - set_point) / (1000.0 / GYRO_UPDATE_INTERVAL);
            }

        }
    }
}
