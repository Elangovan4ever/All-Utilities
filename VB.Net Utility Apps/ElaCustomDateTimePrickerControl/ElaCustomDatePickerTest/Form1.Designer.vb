<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class Form1
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Me.DateTimePicker1 = New System.Windows.Forms.DateTimePicker()
        Me.ElaCustomDateTimePicker1 = New ElaCustomDateTimePrickerControl.ElaCustomDateTimePicker()
        Me.ElaCustomDateTimePicker2 = New ElaCustomDateTimePrickerControl.ElaCustomDateTimePicker()
        Me.SuspendLayout()
        '
        'DateTimePicker1
        '
        Me.DateTimePicker1.CustomFormat = "    m"
        Me.DateTimePicker1.Location = New System.Drawing.Point(283, 118)
        Me.DateTimePicker1.Name = "DateTimePicker1"
        Me.DateTimePicker1.Size = New System.Drawing.Size(200, 20)
        Me.DateTimePicker1.TabIndex = 1
        '
        'ElaCustomDateTimePicker1
        '
        Me.ElaCustomDateTimePicker1.ArrowSquareColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomDateTimePicker1.ArrowSquareColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomDateTimePicker1.ArrowSquareColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomDateTimePicker1.ArrowTriangleColor = System.Drawing.Color.Gray
        Me.ElaCustomDateTimePicker1.ArrowTriangleColorFocus = System.Drawing.Color.Gray
        Me.ElaCustomDateTimePicker1.ArrowTriangleColorMouseEnter = System.Drawing.Color.White
        Me.ElaCustomDateTimePicker1.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomDateTimePicker1.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomDateTimePicker1.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomDateTimePicker1.BorderThickness = ElaCustomDateTimePrickerControl.ElaCustomDateTimePicker.BorderThicknessEnum.Thick
        Me.ElaCustomDateTimePicker1.Location = New System.Drawing.Point(283, 48)
        Me.ElaCustomDateTimePicker1.Name = "ElaCustomDateTimePicker1"
        Me.ElaCustomDateTimePicker1.Size = New System.Drawing.Size(200, 20)
        Me.ElaCustomDateTimePicker1.TabIndex = 0
        '
        'ElaCustomDateTimePicker2
        '
        Me.ElaCustomDateTimePicker2.ArrowSquareColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomDateTimePicker2.ArrowSquareColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomDateTimePicker2.ArrowSquareColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomDateTimePicker2.ArrowTriangleColor = System.Drawing.Color.Gray
        Me.ElaCustomDateTimePicker2.ArrowTriangleColorFocus = System.Drawing.Color.Gray
        Me.ElaCustomDateTimePicker2.ArrowTriangleColorMouseEnter = System.Drawing.Color.White
        Me.ElaCustomDateTimePicker2.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomDateTimePicker2.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomDateTimePicker2.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomDateTimePicker2.BorderThickness = ElaCustomDateTimePrickerControl.ElaCustomDateTimePicker.BorderThicknessEnum.Thick
        Me.ElaCustomDateTimePicker2.Location = New System.Drawing.Point(283, 85)
        Me.ElaCustomDateTimePicker2.Name = "ElaCustomDateTimePicker2"
        Me.ElaCustomDateTimePicker2.Size = New System.Drawing.Size(200, 20)
        Me.ElaCustomDateTimePicker2.TabIndex = 2
        '
        'Form1
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(1111, 536)
        Me.Controls.Add(Me.ElaCustomDateTimePicker2)
        Me.Controls.Add(Me.DateTimePicker1)
        Me.Controls.Add(Me.ElaCustomDateTimePicker1)
        Me.Name = "Form1"
        Me.Text = "Form1"
        Me.ResumeLayout(False)

    End Sub

    Friend WithEvents ElaCustomDateTimePicker1 As ElaCustomDateTimePrickerControl.ElaCustomDateTimePicker
    Friend WithEvents DateTimePicker1 As DateTimePicker
    Friend WithEvents ElaCustomDateTimePicker2 As ElaCustomDateTimePrickerControl.ElaCustomDateTimePicker
End Class
