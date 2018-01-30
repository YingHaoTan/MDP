void angleControl(float angle_desired, Motor *motorValue, bool *aligned, bool forward, float angleThreshold)
{
    static float iTerm = 0;
    static uint32_t lastTimestamp = 0;
    float timeDelta = ((float)(HAL_GetTick() - lastTimestamp))/1000.0f;

    lastTimestamp = HAL_GetTick();

    static int counter = 0;
    static float lastAngleDiff = 0.0f;
    static int alignCounter = 0;

    int32_t motor1, motor2;
    float angleDiff = (angle_desired - *getRobotAngle()) * PI / 180.0f;

    //improve that...
    if (forward == true)
        angleDiff = atan2f(sinf(angleDiff), cosf(angleDiff));
    else
    {
        angleDiff = tanf(atan2f(sinf(angleDiff), cosf(angleDiff)));
        maximumf(&angleDiff, 5.0f);
    }
//    float PGain = 20.0f;
//    float DGain = 2.5f;
//    float IGain = 1.0f;
//
//    float pTerm = 0.0f;
//    float dTerm = 0.0f;
//    static float iTerm = 0.0f;
//    pTerm = angleDiff;
//    dTerm = (angleDiff - lastAngleDiff) / timeDelta; //(0.01367f);
//    iTerm += angleDiff / timeDelta;//2.0f * (0.01367f);

    float pTerm = angleDiff;
    float dTerm = (angleDiff - lastAngleDiff) / (0.01367f);
    iTerm = iTerm + (angleDiff + lastAngleDiff) / 2.0f * (0.01367f);

    float PGain = 20.0f;
    float DGain = 1.5f;
    float IGain = 3.0f;

    if (iTerm > 10.0f)
    {
      iTerm = 10.0f;
    }

    if (iTerm > 10.0f) iTerm = 10.0f;
    else if (iTerm < -10.0f) iTerm = -10.0f;

    if (abs(angleDiff * 180.0f / PI) > angleThreshold)
    {
        float value = PGain * pTerm + DGain * dTerm + IGain * iTerm;
        if (angleDiff < 0.0f)
        {
            motor1 = value;
            motor2 = -value;
        }
        else
        {
            motor1 = value;
            motor2 = -value;
        }
    }
    else
    {
        motor1 = 0;
        motor2 = 0;
        alignCounter++;
    }

    lastAngleDiff = angleDiff;
    motorValue->motor1 = motor1;
    motorValue->motor2 = motor2;

    if (alignCounter > 10)
    {
        *aligned = true;
        alignCounter = 0;
        iTerm = 0;
    }

//    if (counter > 100)
//    {
//      Position *pos = getRobotPosition();
//      if (DEBUG_ENABLED()) {
//        debug_printf("New Position: position= %d %d | orientation= %f\n", pos->x, pos->y, *getRobotAngle());
//        debug_printf("pTerm= %f | dTerm= %f | iTerm= %f \n", pTerm, dTerm, iTerm);
//        debug_printf("angle: motor1= %d | motor2= %d\n", motor1, motor2);
//      }
//      counter = 0;
//    }
//    else
//    {
//      counter++;
//    }
}